package logica.grafico_pedido

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
import persistencia.entidades.HiloStockEntity
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
 * Además, devuelve el gráfico modificado al activity que lo llamó.
 *
 * - Se oculta el campo "count de tela" después de haberlo seleccionado una vez por gráfico.
 * - Al pulsar sobre el nombre de un hilo, se consulta el stock actual en Room y se muestra en pantalla.
 *
 * * @author Olga y Sandra Macías Aragón
 */
class GraficoPedido : BaseActivity() {

    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private lateinit var daoGrafico: HiloGraficoDao
    private lateinit var daoStock: HiloStockDao
    private lateinit var txtTotal: TextView
    private lateinit var txtStockActual: TextView

    private var graficoNombre: String = ""
    private var graficoId: Int = -1
    private var countTelaGlobal: Int? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this)

        // Inicializa DAOs y sesión
        daoGrafico = ThreadlyDatabase.getDatabase(applicationContext).hiloGraficoDao()
        daoStock = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        // Referencias de vistas
        txtTotal = findViewById(R.id.txtVw_totalMadejasGraficoIndividual)
        txtStockActual = findViewById(R.id.txtVw_stockHiloActual)

        // Recibimos el nombre del gráfico desde el Intent
        val graficoRecibido = intent.getSerializableExtra("grafico") as? logica.pedido_hilos.Grafico
        if (graficoRecibido == null) {
            Toast.makeText(this, "Error: gráfico no recibido", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        graficoNombre = graficoRecibido.nombre
        findViewById<TextView>(R.id.txtVw_cabeceraGrafico).text = graficoNombre

        // Inicializamos o recuperamos el gráfico en la BBDD
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                var existingId = daoGrafico.obtenerIdPorNombre(graficoNombre)
                if (existingId == null) {
                    // Insertar nueva fila en tabla "graficos"
                    val nuevo = GraficoEntity(nombre = graficoNombre)
                    val newRowId = daoGrafico.insertarGrafico(nuevo).toInt()
                    existingId = newRowId
                }
                graficoId = existingId ?: -1
            }
            if (graficoId < 0) {
                Toast.makeText(this@GraficoPedido, "Error cargando gráfico", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            // Una vez tenemos graficoId, configuramos RecyclerView y botones
            configurarRecycler()
            configurarBotones()
        }
    }

    /**
     * Configura el RecyclerView con el Adaptador, cargando los hilos existentes en este gráfico.
     * También inicializa el texto del total de madejas.
     */
    private fun configurarRecycler() {
        val recyclerView = findViewById<RecyclerView>(R.id.tabla_grafico)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            // 1) Recuperar lista de HiloGraficoEntity desde Room
            val entidades = withContext(Dispatchers.IO) {
                daoGrafico.obtenerHilosPorGrafico(graficoId)
            }

            // 2) Mapear a dominio HiloGrafico (solo Int en madejas)
            val listaDominio = entidades.map {
                HiloGrafico(it.hilo, it.madejas)
            }
                // 3) Ordenar por código de hilo usando la función de utilidad
                .let { ordenarHilos(it) { h -> h.hilo } }
                .toMutableList()

            // 4) Inicializar AdaptadorGrafico con listener de clic para mostrar stock
            adaptadorGrafico = AdaptadorGrafico(
                listaDominio,
                onClickHilo = { hiloGrafico ->
                    // Al hacer clic sobre el nombre, mostramos stock actual en txtStockActual:
                    lifecycleScope.launch {
                        val stock = withContext(Dispatchers.IO) {
                            daoStock.obtenerMadejas(userId, hiloGrafico.hilo)
                        }
                        txtStockActual.text = stock?.toString() ?: "-"
                    }
                },
                onLongClickHilo = ::dialogBorrarHilo,
                hiloResaltado = null
            ) { total ->
                // Actualiza el total de madejas cada vez que cambie
                txtTotal.text = "Total Madejas: $total"
            }

            // 5) Conectar Adaptador al RecyclerView
            recyclerView.adapter = adaptadorGrafico

            // 6) Mostrar total inicial
            txtTotal.text = "Total Madejas: ${listaDominio.sumOf { it.madejas }}"

            // 7) Inicializar buscador (se apoya en adaptador)
            buscadorHilo()
        }
    }

    /**
     * Configura los botones de "Agregar hilo" y "Volver y devolver resultado".
     */
    private fun configurarBotones() {
        findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual)
            .setOnClickListener { dialogAgregarHiloGrafico() }

        findViewById<Button>(R.id.btn_volver_pedido_desde_grafico)
            .setOnClickListener { devolverResultadoYSalir() }
    }

    /**
     * Implementa la búsqueda interna en el RecyclerView.
     * Resalta el hilo si se encuentra o muestra mensaje de "Sin resultados".
     */
    private fun buscadorHilo() {
        val edt = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        val btn = findViewById<ImageView>(R.id.imgVw_lupaGrafico)
        val rv = findViewById<RecyclerView>(R.id.tabla_grafico)
        val txtNo = findViewById<TextView>(R.id.txtVw_sinResultadosGrafico)
        txtNo.visibility = View.GONE

        btn.setOnClickListener {
            val code = edt.text.toString().trim().uppercase()
            val listaAct = adaptadorGrafico.obtenerLista()
            val found = listaAct.find { it.hilo == code }
            if (found != null) {
                adaptadorGrafico.resaltarHilo(found.hilo)
                adaptadorGrafico.actualizarLista(listaAct)
                rv.scrollToPosition(listaAct.indexOf(found))
                txtNo.visibility = View.GONE
                rv.visibility = View.VISIBLE
            } else {
                txtNo.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        }

        edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorGrafico.resaltarHilo(null)
                    adaptadorGrafico.actualizarLista(adaptadorGrafico.obtenerLista())
                    txtNo.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Muestra un diálogo para agregar un nuevo hilo al gráfico.
     * - Solo permite agregar si el hilo existe en el catálogo del usuario.
     * - Pide "puntadas" y "count de tela" la primera vez; luego oculta count.
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

        // Si ya se seleccionó countTelaGlobal para este gráfico, ocultamos el campo
        if (countTelaGlobal != null) {
            inpC.visibility = View.GONE
        }

        btnG.setOnClickListener {
            val hiloCode = inpH.text.toString().trim().uppercase()
            val punt = inpP.text.toString().trim().toIntOrNull()
            val count = countTelaGlobal
                ?: inpC.text.toString().trim().toIntOrNull()
                    ?.takeIf { it in listOf(14, 16, 18, 20, 25) }
                    ?.also { countTelaGlobal = it }

            if (punt == null || count == null) {
                Toast.makeText(this, "Campos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calculamos madejas (entero)
            val madejas = calcularMadejas(punt, count)

            lifecycleScope.launch {
                // Antes de insertar, validamos que el hilo esté en el catálogo del usuario
                val existeEnCatalogo = withContext(Dispatchers.IO) {
                    ThreadlyDatabase.getDatabase(applicationContext)
                        .hiloCatalogoDao()
                        .obtenerHiloPorNumYUsuario(hiloCode, userId) != null
                }
                if (!existeEnCatalogo) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@GraficoPedido,
                            "El hilo no está en tu catálogo. Añádelo primero.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Insertamos en Room la nueva fila en hilos_grafico
                withContext(Dispatchers.IO) {
                    daoGrafico.insertarHiloEnGrafico(
                        HiloGraficoEntity(
                            graficoId = graficoId,
                            hilo = hiloCode,
                            madejas = madejas
                        )
                    )
                }
                // Volvemos a recargar la lista
                withContext(Dispatchers.Main) {
                    configurarRecycler()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    /**
     * Muestra un diálogo de confirmación para borrar un hilo del gráfico.
     * Si se confirma, elimina la fila correspondiente en Room y recarga el RecyclerView.
     */
    private fun dialogBorrarHilo(h: HiloGrafico) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.pedidob_dialog_borrar_hilo)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            ajustarDialog(this)
        }
        val btnConf = dialog.findViewById<Button>(R.id.btn_guardarHilo_dialog_deleteHilo)
        val btnVol = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_deleteHilo)
        val txtMsg = dialog.findViewById<TextView>(R.id.txtVw_textoInfo_dialog_deleteHilo)

        // Construir mensaje resaltando en rojo el código del hilo
        val plantilla = getString(R.string.textoInfo_dialog_deleteHilo)
        val texto = plantilla.replace("%s", h.hilo)
        txtMsg.text = SpannableString(texto).apply {
            val start = texto.indexOf(h.hilo)
            val end = start + h.hilo.length
            setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        btnVol.setOnClickListener { dialog.dismiss() }
        btnConf.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    daoGrafico.eliminarHiloDeGrafico(graficoId, h.hilo)
                }
                withContext(Dispatchers.Main) {
                    configurarRecycler()
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    /**
     * Devuelve el gráfico actualizado (nombre + lista de hilos) al Activity que lo llamó y termina.
     */
    private fun devolverResultadoYSalir() {
        val listaFinal = adaptadorGrafico.obtenerLista()
        val resultado = logica.pedido_hilos.Grafico(
            nombre = graficoNombre,
            listaHilos = listaFinal
        )
        setResult(RESULT_OK, Intent().apply {
            putExtra("grafico", resultado)
            putExtra("position", intent.getIntExtra("position", -1))
        })
        finish()
    }
}
