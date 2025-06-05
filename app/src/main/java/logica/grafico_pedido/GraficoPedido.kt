package logica.grafico_pedido

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloGraficoDao
import persistencia.daos.HiloStockDao
import persistencia.entidades.GraficoEntity
import persistencia.entidades.HiloGraficoEntity
import logica.pedido_hilos.Grafico
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog
import utiles.funciones.calcularMadejas
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

/**
 * Actividad que gestiona la visualización y edición de un gráfico individual dentro de un pedido.
 * Permite mostrar la lista de hilos asociados al gráfico, agregar nuevos hilos,
 * eliminar hilos existentes, buscar hilos en la lista y mostrar el stock disponible.
 *
 * - Se oculta el campo "count de tela" después de haberlo introducido por primera vez.
 * - Al pulsar sobre el nombre de un hilo, se consulta el stock actual en Room y se muestra en pantalla.
 *
 * Cuando se pulsa “volver”, este activity devuelve al caller (PedidoHilos) un objeto Grafico
 * con su lista de HiloGrafico (incluyendo madejas y lista actualizada).
 */
class GraficoPedido : BaseActivity() {

    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private lateinit var daoGrafico: HiloGraficoDao
    private lateinit var daoStock: HiloStockDao
    private lateinit var txtTotal: TextView
    private lateinit var txtStockActual: TextView
    private lateinit var recyclerView: RecyclerView

    // Lista en memoria de HiloGrafico (dominio)
    private var listaDominio: MutableList<HiloGrafico> = mutableListOf()

    private var graficoNombre: String = ""
    private var graficoId: Int = -1

    // countTelaGlobal se mantiene para ocultar el campo tras la primera inserción
    private var countTelaGlobal: Int? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this)

        daoGrafico = ThreadlyDatabase.getDatabase(applicationContext).hiloGraficoDao()
        daoStock = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        txtTotal = findViewById(R.id.txtVw_totalMadejasGraficoIndividual)
        txtStockActual = findViewById(R.id.txtVw_stockHiloActual)
        recyclerView = findViewById(R.id.tabla_grafico)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Recibimos el objeto Grafico desde PedidoHilos
        val graficoRecibido = intent.getSerializableExtra("grafico") as? Grafico
        if (graficoRecibido == null) {
            Toast.makeText(this, "Error: gráfico no recibido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        graficoNombre = graficoRecibido.nombre
        findViewById<TextView>(R.id.txtVw_cabeceraGrafico).text = graficoNombre

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                var existingId = daoGrafico.obtenerIdPorNombre(graficoNombre)
                if (existingId == null) {
                    existingId = daoGrafico.insertarGrafico(
                        GraficoEntity(
                            nombre = graficoNombre,
                            idPedido = null,
                            userId = userId
                        )
                    ).toInt()
                }
                graficoId = existingId

            }

            if (graficoId < 0) {
                Toast.makeText(this@GraficoPedido, "Error cargando gráfico", Toast.LENGTH_SHORT)
                    .show()
                finish()
                return@launch
            }

            configurarRecycler()
            configurarBotones()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun configurarRecycler() {
        lifecycleScope.launch {
            val entidades = withContext(Dispatchers.IO) {
                daoGrafico.obtenerHilosPorGrafico(graficoId)
            }

            // Mapeamos entidades a dominio y ordenamos
            listaDominio = entidades.map { HiloGrafico(it.hilo, it.madejas) }
                .let { ordenarHilos(it) { h -> h.hilo } }
                .toMutableList()

            if (!::adaptadorGrafico.isInitialized) {
                adaptadorGrafico = AdaptadorGrafico(
                    listaDominio,
                    onClickHilo = { hiloGrafico ->
                        val hiloActual = hiloGrafico.hilo
                        val yaResaltado = adaptadorGrafico.obtenerHiloResaltadoClick() == hiloActual
                        adaptadorGrafico.resaltarHiloClick(hiloActual)
                        if (yaResaltado) {
                            txtStockActual.text = "Stock: 0"
                        } else {
                            mostrarStock(hiloActual)
                        }
                    },
                    onLongClickHilo = ::dialogBorrarHilo,
                    onTotalChanged = { total ->
                        txtTotal.text = "Total Madejas: $total"
                    },
                    onUpdateMadejas = { hiloGrafico ->
                        // Persistir la nueva cantidad de madejas en Room
                        lifecycleScope.launch(Dispatchers.IO) {
                            daoGrafico.actualizarMadejas(hiloGrafico.hilo, hiloGrafico.madejas)
                        }
                    }
                )
                recyclerView.adapter = adaptadorGrafico
            } else {
                adaptadorGrafico.actualizarLista(listaDominio)
            }

            // Calculamos y mostramos el total inicial
            val total = listaDominio.sumOf { it.madejas }
            txtTotal.text = "Total Madejas: $total"
            buscadorGrafico()
        }
    }

    private fun configurarBotones() {
        findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual).setOnClickListener {
            dialogAgregarHiloGrafico()
        }
        findViewById<Button>(R.id.btn_volver_pedido_desde_grafico).setOnClickListener {
            devolverResultadoYSalir()
        }
    }

    private fun buscadorGrafico() {

        val edt = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        val btn = findViewById<ImageView>(R.id.imgVw_lupaGrafico)
        val txtNo = findViewById<TextView>(R.id.txtVw_sinResultadosGrafico)

        txtNo.visibility = View.GONE

        btn.setOnClickListener {

            // 1) Ocultar el teclado
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edt.windowToken, 0)
            val code = edt.text.toString().trim().uppercase()
            val found = listaDominio.find { it.hilo == code }
            if (found != null) {
                adaptadorGrafico.resaltarHiloBusqueda(found.hilo)
                adaptadorGrafico.actualizarLista(listaDominio)
                recyclerView.scrollToPosition(listaDominio.indexOf(found))
                txtNo.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                txtNo.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }

        edt.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorGrafico.resaltarHiloBusqueda(null)
                    adaptadorGrafico.actualizarLista(listaDominio)
                    txtNo.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun mostrarStock(hilo: String) {
        lifecycleScope.launch {
            val stock = withContext(Dispatchers.IO) {
                daoStock.obtenerMadejas(userId, hilo)
            }
            txtStockActual.text = "Stock: ${stock ?: 0}"
        }
    }

    /**
     * Dialog para añadir un hilo al gráfico. Solo pide “count de tela” la primera vez.
     * Además, si el hilo ya existe en este gráfico, muestra un Toast y no lo inserta.
     */
    private fun dialogAgregarHiloGrafico() {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.pedidob_dialog_agregar_hilo)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            ajustarDialog(this)
            setCancelable(false)
        }

        val inpH = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inpP = dialog.findViewById<EditText>(R.id.edTxt_introducirPuntadas_dialog_addHilo)
        val inpC = dialog.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnG = dialog.findViewById<Button>(R.id.btn_guardar_dialog_pedidob_addHilo)

        dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_addHilo)
            .setOnClickListener { dialog.dismiss() }

        // 1) Si countTelaGlobal ya existe, ocultamos el campo completo (solo se pide una vez)
        if (countTelaGlobal != null) {
            inpC.visibility = View.GONE
        } else {
            inpC.visibility = View.VISIBLE
        }

        btnG.setOnClickListener {
            val hiloCode = inpH.text.toString().trim().uppercase()
            val punt      = inpP.text.toString().trim().toIntOrNull()

            // 2) Si el hilo ya está en listaDominio, mostramos Toast y salimos
            if (listaDominio.any { it.hilo == hiloCode }) {
                Toast.makeText(this, "El hilo ya se ha añadido al gráfico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3) Validamos campos obligatorios: hiloCode y punt deben ser no nulos
            if (hiloCode.isEmpty() || punt == null) {
                Toast.makeText(this, "Campos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4) Ahora comprobamos existencia en catálogo ANTES de manejar el countTela
            lifecycleScope.launch {
                val existeEnCatalogo = withContext(Dispatchers.IO) {
                    ThreadlyDatabase.getDatabase(applicationContext)
                        .hiloCatalogoDao()
                        .obtenerHiloPorNumYUsuario(hiloCode, userId) != null
                }

                if (!existeEnCatalogo) {
                    // Si no existe en catálogo, mostramos Toast y salimos: NO tocamos countTelaGlobal
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@GraficoPedido,
                            "El hilo no está en tu catálogo. Añádelo primero.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // 5) A estas alturas sabemos que el hilo existe. Ahora sí tomamos el countTela,
                //    Y SI countTelaGlobal es null, leemos el valor y lo guardamos.
                val countIntroducido: Int? = if (countTelaGlobal == null) {
                    inpC.text.toString().trim().toIntOrNull()?.takeIf { it in listOf(14, 16, 18, 20, 25) }
                        ?.also { countTelaGlobal = it }
                } else {
                    countTelaGlobal
                }

                if (countIntroducido == null) {
                    // Si no hay count válido (ni global ni ingreso actual), error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GraficoPedido, "Count de tela inválido", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 6) Calculamos madejas y grabamos en BD
                val madejas = calcularMadejas(punt, countIntroducido)
                withContext(Dispatchers.IO) {
                    daoGrafico.insertarHiloEnGrafico(
                        HiloGraficoEntity(
                            graficoId = graficoId,
                            hilo = hiloCode,
                            madejas = madejas
                        )
                    )
                }

                // 7) Tras insertar, recargamos la lista y cerramos el diálogo en el hilo principal
                withContext(Dispatchers.Main) {
                    configurarRecycler()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun dialogBorrarHilo(h: HiloGrafico) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.pedidob_dialog_borrar_hilo)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            ajustarDialog(this)
        }

        val btnConf = dialog.findViewById<Button>(R.id.btn_guardarHilo_dialog_deleteHilo)
        val btnVol  = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_deleteHilo)
        val txtMsg  = dialog.findViewById<TextView>(R.id.txtVw_textoInfo_dialog_deleteHilo)

        // Pintamos el nombre “en rojo” dentro del mensaje
        val texto = getString(R.string.textoInfo_dialog_deleteHilo).replace("%s", h.hilo)
        txtMsg.text = SpannableString(texto).apply {
            val start = texto.indexOf(h.hilo)
            val end   = start + h.hilo.length
            setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        btnVol.setOnClickListener { dialog.dismiss() }
        btnConf.setOnClickListener {
            lifecycleScope.launch {
                // 1) Borramos de Room
                withContext(Dispatchers.IO) {
                    daoGrafico.eliminarHiloDeGrafico(graficoId, h.hilo)
                }

                // 2) Recargamos la lista
                withContext(Dispatchers.Main) {
                    configurarRecycler()

                    // 3) Si tras recargar no queda ningún hilo, reseteamos countTelaGlobal
                    if (listaDominio.isEmpty()) {
                        countTelaGlobal = null
                    }

                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    /**
     * Devuelve al activity padre (PedidoHilos) el gráfico completo, con su lista de HiloGrafico
     */
    private fun devolverResultadoYSalir() {
        val listaFinal = adaptadorGrafico.obtenerLista()
        val resultado = Grafico(
            nombre = graficoNombre,
            listaHilos = listaFinal
        )
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra("grafico", resultado)
                putExtra("position", intent.getIntExtra("position", -1))
            }
        )
        finish()
    }
}
