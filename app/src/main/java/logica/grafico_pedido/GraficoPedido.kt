package logica.grafico_pedido

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
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
        daoStock   = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        userId     = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        txtTotal       = findViewById(R.id.txtVw_totalMadejasGraficoIndividual)
        txtStockActual = findViewById(R.id.txtVw_stockHiloActual)
        recyclerView   = findViewById(R.id.tabla_grafico)
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
                    existingId = daoGrafico.insertarGrafico(GraficoEntity(nombre = graficoNombre, idPedido = null, userId = userId )).toInt()
                }
                graficoId = existingId
            }

            if (graficoId < 0) {
                Toast.makeText(this@GraficoPedido, "Error cargando gráfico", Toast.LENGTH_SHORT).show()
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
                        lifecycleScope.launch(Dispatchers.IO) {
                            val nuevoValor = hiloGrafico.cantidadModificar ?: hiloGrafico.madejas
                            daoGrafico.actualizarMadejas(hiloGrafico.hilo, nuevoValor)
                            // Actualizar la lista en memoria en Main thread
                            withContext(Dispatchers.Main) {
                                // Buscar el hilo en listaDominio y actualizar madejas
                                val index = listaDominio.indexOfFirst { it.hilo == hiloGrafico.hilo }
                                if (index != -1) {
                                    listaDominio[index] = hiloGrafico.copy(madejas = nuevoValor)
                                    adaptadorGrafico.notifyItemChanged(index)
                                    // Recalcular total y actualizar texto
                                    val total = listaDominio.sumOf { it.madejas }
                                    txtTotal.text = "Total Madejas: $total"
                                }
                            }
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
            buscadorHilo()
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

    private fun buscadorHilo() {
        val edt = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        val btn = findViewById<ImageView>(R.id.imgVw_lupaGrafico)
        val txtNo = findViewById<TextView>(R.id.txtVw_sinResultadosGrafico)

        txtNo.visibility = View.GONE

        btn.setOnClickListener {
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

        edt.addTextChangedListener(object : TextWatcher {
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
            val punt = inpP.text.toString().trim().toIntOrNull()

            // 2) Si countTelaGlobal es null, pedimos de inpC; si ya existe, lo volvemos a usar
            val count = countTelaGlobal ?: inpC.text.toString().trim().toIntOrNull()
                ?.takeIf { it in listOf(14, 16, 18, 20, 25) }
                ?.also { countTelaGlobal = it }

            // 3) Si el hilo ya está en listaDominio, mostramos Toast y salimos
            if (listaDominio.any { it.hilo == hiloCode }) {
                Toast.makeText(this, "El hilo ya se ha añadido al gráfico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (hiloCode.isEmpty() || punt == null || count == null) {
                Toast.makeText(this, "Campos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val madejas = calcularMadejas(punt, count)

            lifecycleScope.launch {
                val existe = withContext(Dispatchers.IO) {
                    ThreadlyDatabase.getDatabase(applicationContext)
                        .hiloCatalogoDao()
                        .obtenerHiloPorNumYUsuario(hiloCode, userId) != null
                }

                if (!existe) {
                    Toast.makeText(
                        this@GraficoPedido,
                        "El hilo no está en tu catálogo. Añádelo primero.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    daoGrafico.insertarHiloEnGrafico(
                        HiloGraficoEntity(
                            graficoId = graficoId,
                            hilo = hiloCode,
                            madejas = madejas
                        )
                    )
                }

                // 4) Tras insertar, recargamos la lista y cerramos el diálogo
                configurarRecycler()
                dialog.dismiss()
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
        val btnVol = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_deleteHilo)
        val txtMsg = dialog.findViewById<TextView>(R.id.txtVw_textoInfo_dialog_deleteHilo)

        val texto = getString(R.string.textoInfo_dialog_deleteHilo).replace("%s", h.hilo)
        txtMsg.text = SpannableString(texto).apply {
            val start = texto.indexOf(h.hilo)
            val end = start + h.hilo.length
            setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        btnVol.setOnClickListener { dialog.dismiss() }
        btnConf.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    daoGrafico.eliminarHiloDeGrafico(graficoId, h.hilo)
                }
                configurarRecycler()
                dialog.dismiss()
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
