package logica.grafico_pedido

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import logica.catalogo_hilos.CatalogoSingleton
import logica.pedido_hilos.Grafico
import utiles.BaseActivity
import utiles.funciones.*

/**
 * Actividad que gestiona la visualización y edición de un gráfico individual dentro de un pedido.
 * Permite mostrar la lista de hilos asociados al gráfico, agregar nuevos hilos,
 * eliminar hilos existentes, buscar hilos en la lista y mostrar el stock disponible.
 *
 * Además, devuelve el gráfico modificado al activity que lo llamó.
 *
 * @author Olga y Sandra Macías Aragón
 */
class GraficoPedido : BaseActivity() {

    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private var countTelaGlobal: Int? = null

    /**
     * Método llamado al crear la actividad.
     * Inicializa la UI, carga el gráfico recibido y configura el RecyclerView y buscador.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this)

        val txtTotal = findViewById<TextView>(R.id.txtVw_totalMadejasGraficoIndividual)

        val graficoRecibido = intent.getSerializableExtra("grafico") as? Grafico
        if (graficoRecibido == null) {
            Log.e("GraficoPedido", "El gráfico recibido es nulo.")
            Toast.makeText(this, "Error: gráfico no recibido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        /* establece el gráfico recibido en el singleton para manipularlo */
        GraficoSingleton.setGrafico(graficoRecibido)

        /* actualiza la cabecera con el nombre del gráfico */
        findViewById<TextView>(R.id.txtVw_cabeceraGrafico).text = GraficoSingleton.grafico?.nombre

        /* configura RecyclerView y su adaptador con la lista de hilos del gráfico */
        val recyclerView = findViewById<RecyclerView>(R.id.tabla_grafico)
        adaptadorGrafico = AdaptadorGrafico(
            GraficoSingleton.getListaHilos().toMutableList(),
            onClickHilo = { hilo ->
                /* al pulsar un hilo muestra su stock actual */
                val txtVwStock = findViewById<TextView>(R.id.txtVw_stockHiloActual)
               // val stock = StockSingleton.obtenerMadejas(hilo.hilo.uppercase())?.toString() ?: "-"
                //txtVwStock.text = getString(R.string.stockHiloActual, stock)
            },
            onLongClickHilo = ::dialogBorrarHilo,
            hiloResaltado = null,
            onTotalChanged = { total ->
                /* actualiza el total de madejas en la UI */
                txtTotal.text = "Total Madejas: $total"
            }
        )

        recyclerView.adapter = adaptadorGrafico
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* configura botón para agregar nuevo hilo */
        findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual).setOnClickListener {
            dialogAgregarHiloGrafico()
        }
        /* botón para volver y devolver resultado al activity anterior */
        findViewById<Button>(R.id.btn_volver_pedido_desde_grafico).setOnClickListener {
            devolverResultadoYSalir()
        }

        /* configura buscador de hilos en el gráfico */
        buscadorHilo()
    }

    /**
     * Configura la funcionalidad del buscador de hilos en la lista del gráfico.
     * Permite buscar un hilo exacto por su código y resaltar el resultado,
     * o mostrar mensaje de "sin resultados" si no se encuentra.
     */
    private fun buscadorHilo() {
        val buscarGrafico = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        val btnLupaGrafico = findViewById<ImageView>(R.id.imgVw_lupaGrafico)
        val tablaGrafico = findViewById<RecyclerView>(R.id.tabla_grafico)
        val txtNoResultadosGrafico = findViewById<TextView>(R.id.txtVw_sinResultadosGrafico)

        txtNoResultadosGrafico.visibility = View.GONE

        btnLupaGrafico.setOnClickListener {
            val texto = buscarGrafico.text.toString().trim().uppercase()
            val listaActual = GraficoSingleton.getListaHilos()
            val coincidencia = listaActual.find { it.hilo.uppercase() == texto }

            if (coincidencia != null) {
                adaptadorGrafico.resaltarHilo(coincidencia.hilo)
                adaptadorGrafico.actualizarLista(listaActual)
                tablaGrafico.visibility = View.VISIBLE
                txtNoResultadosGrafico.visibility = View.GONE
                tablaGrafico.scrollToPosition(listaActual.indexOf(coincidencia))
            } else {
                tablaGrafico.visibility = View.GONE
                txtNoResultadosGrafico.visibility = View.VISIBLE
            }
        }

        buscarGrafico.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    val listaActual = GraficoSingleton.getListaHilos()
                    adaptadorGrafico.resaltarHilo(null)
                    adaptadorGrafico.actualizarLista(listaActual)
                    tablaGrafico.visibility = View.VISIBLE
                    txtNoResultadosGrafico.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Muestra un diálogo personalizado para agregar un nuevo hilo al gráfico.
     * Valida que el hilo exista en el catálogo y que los campos sean correctos,
     * calcula las madejas necesarias y actualiza la lista y el adaptador.
     */
    private fun dialogAgregarHiloGrafico() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedidob_dialog_agregar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val hiloGrafico = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val puntadasGrafico = dialog.findViewById<EditText>(R.id.edTxt_introducirPuntadas_dialog_addHilo)
        val countTela = dialog.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardar_dialog_pedidob_addHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_addHilo)

        if (countTelaGlobal != null) {
            countTela.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            try {
                val nombreHilo = hiloGrafico.text.toString().trim().uppercase()
                if (!CatalogoSingleton.listaCatalogo.any { it.numHilo == nombreHilo }) {
                    Toast.makeText(this, "El hilo no se encuentra en el catálogo. Añádelo primero.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val puntadasInt = puntadasGrafico.text.toString().trim().toIntOrNull()
                val countTelaInt: Int? = countTelaGlobal ?: countTela.text.toString().trim().toIntOrNull()?.takeIf {
                    it in listOf(14, 16, 18, 20, 25)
                }?.also { countTelaGlobal = it }

                if (puntadasInt == null || puntadasInt <= 0 || countTelaInt == null) {
                    Toast.makeText(this, "Campos inválidos o incompletos.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val madejas = calcularMadejas(puntadasInt, countTelaInt)
                GraficoSingleton.agregarHilo(nombreHilo, madejas)

                val listaOrdenada = GraficoSingleton.getListaHilos()
                adaptadorGrafico.actualizarLista(listaOrdenada)
                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnVolver.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Muestra un diálogo de confirmación para borrar un hilo del gráfico.
     * Resalta el código del hilo en rojo en el mensaje para advertir.
     * Si se confirma, elimina el hilo y actualiza la lista.
     *
     * @param hilo Hilo a eliminar.
     */
    private fun dialogBorrarHilo(hilo: HiloGrafico) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedidob_dialog_borrar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)

        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_deleteHilo)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_guardarHilo_dialog_deleteHilo)
        val txtMensaje = dialog.findViewById<TextView>(R.id.txtVw_textoInfo_dialog_deleteHilo)

        val texto = getString(R.string.textoInfo_dialog_deleteHilo).replace("%s", hilo.hilo)
        val spannable = SpannableString(texto).apply {
            val start = texto.indexOf(hilo.hilo)
            val end = start + hilo.hilo.length
            setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        txtMensaje.text = spannable

        btnVolver.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            GraficoSingleton.eliminarHilo(hilo)
            adaptadorGrafico.actualizarLista(GraficoSingleton.getListaHilos())
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Devuelve el gráfico actualizado al activity que lo llamó y finaliza esta actividad.
     * Incluye el gráfico modificado y la posición original para identificarlo.
     */
    private fun devolverResultadoYSalir() {
        GraficoSingleton.actualizarTotalMadejas()
        val resultIntent = Intent().apply {
            putExtra("grafico", GraficoSingleton.grafico)
            putExtra("position", intent.getIntExtra("position", -1))
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
