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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import logica.catalogo_hilos.CatalogoSingleton
import logica.pedido_hilos.Grafico
import logica.stock_personal.StockSingleton
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.funciones.calcularMadejas
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

class GraficoPedido : BaseActivity() {

    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private var grafico: Grafico? = null /* traer el gráfico de la pantalla anterior */

    /* para controlar si es la primera vez que se introduce, o no */
    private var countTelaGlobal: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        val txtTotal = findViewById<TextView>(R.id.txtVw_totalMadejasGraficoIndividual)

        /* para poner como cabecera el nombre del gráfico */
        grafico = intent.getSerializableExtra("grafico") as? Grafico
        if (grafico == null) {
            Log.e("GraficoPedido", "El gráfico recibido es nulo.")
            Toast.makeText(this, "Error: gráfico no recibido", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val cabecera = findViewById<TextView>(R.id.txtVw_cabeceraGrafico)
        cabecera.text = grafico?.nombre

        /* inicialización del recycler view */
        val recyclerView = findViewById<RecyclerView>(R.id.tabla_grafico)
        adaptadorGrafico = AdaptadorGrafico(
            grafico!!.listaHilos.toMutableList(),
            onClickHilo = { hilo ->
                val txtVwStock = findViewById<TextView>(R.id.txtVw_stockHiloActual)
                txtVwStock.text = "Stock: " /* para que no aparezca la expresión del @string */
                val stock = StockSingleton.obtenerMadejas(hilo.hilo.uppercase())?.toString() ?: "-"
                txtVwStock.text = getString(R.string.stockHiloActual, stock)
            },
            onLongClickHilo = ::dialogBorrarHilo, /* callback: eliminar llama al adaptador */
            hiloResaltado = null,
            onTotalChanged = { total ->
                txtTotal.text = "Total Madejas: $total" /* total de madejas para el gráfico */
            }
        )

        recyclerView.adapter = adaptadorGrafico
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* declaración de componentes */
        var btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual)
        var btnVolver = findViewById<Button>(R.id.btn_volver_pedido_desde_grafico)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarHilo.setOnClickListener { dialogAgregarHiloGrafico() }
        btnVolver.setOnClickListener {
            devolverResultadoYSalir()
        }
        /* inicio de las funciones en constante uso */
        buscadorHilo()
    }

    /* buscar un hilo en el gráfico */
    private fun buscadorHilo() {
        val buscarGrafico = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        val btnLupaGrafico = findViewById<ImageView>(R.id.imgVw_lupaGrafico)
        val tablaGrafico = findViewById<RecyclerView>(R.id.tabla_grafico)
        val txtNoResultadosGrafico = findViewById<TextView>(R.id.txtVw_sinResultadosGrafico)

        txtNoResultadosGrafico.visibility = View.GONE

        btnLupaGrafico.setOnClickListener {
            val texto = buscarGrafico.text.toString().trim().uppercase()
            val listaActual = grafico?.listaHilos ?: mutableListOf()
            val coincidencia = grafico?.listaHilos?.find { it.hilo.uppercase() == texto }


            if (coincidencia != null) {
                /* si encuentra el hilo lo resaltará en la tabla */
                adaptadorGrafico.resaltarHilo(coincidencia.hilo)
                adaptadorGrafico.actualizarLista(listaActual)
                tablaGrafico.visibility = View.VISIBLE
                txtNoResultadosGrafico.visibility = View.GONE

                val index = listaActual.indexOf(coincidencia)
                tablaGrafico.scrollToPosition(index)

            } else {
                tablaGrafico.visibility = View.GONE
                txtNoResultadosGrafico.visibility = View.VISIBLE
            }
        }

        /* si se borra la búsqueda la tabla vuelve a aparecer */
        buscarGrafico.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    val listaActual = grafico?.listaHilos ?: mutableListOf()
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

    /* agregar hilo al gráfico */
    private fun dialogAgregarHiloGrafico() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedidob_dialog_agregar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        /* variables del dialog */
        val hiloGrafico = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val puntadasGrafico =
            dialog.findViewById<EditText>(R.id.edTxt_introducirPuntadas_dialog_addHilo)
        val countTela = dialog.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardar_dialog_pedidob_addHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_addHilo)

        /* si el count ya se ha definido para este gráfico, no volverá a pedirlo */
        if (countTelaGlobal != null) {
            countTela.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            try {
                val nombreHilo = hiloGrafico.text.toString().trim().uppercase()
                val hiloEnCatalogo =
                    CatalogoSingleton.listaCatalogo.any { it.numHilo == nombreHilo }
                if (!hiloEnCatalogo) {
                    Toast.makeText(
                        this,
                        "El hilo no se encuentra en el catálogo. Añádelo ahí primero.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                val stringPuntadas = puntadasGrafico.text.toString().trim()
                /* comprobar que los campos estén rellenos */
                if (nombreHilo.isEmpty() || stringPuntadas.isEmpty() ||
                    (countTelaGlobal == null && countTela.text.toString().trim().isEmpty())
                ) {
                    Toast.makeText(
                        this,
                        "Por favor, completa todos los campos.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val puntadasInt = stringPuntadas.toIntOrNull()
                val countTelaInt: Int? = if (countTelaGlobal != null) {
                    countTelaGlobal
                } else {
                    val input = countTela.text.toString().trim().toIntOrNull()
                    /* sólo counts permitidos */
                    if (input != null && input in listOf(14, 16, 18, 20, 25)) {
                        countTelaGlobal = input  /* se guarda para el siguiente hilo */
                        input
                    } else {
                        Toast.makeText(
                            this,
                            "El count de tela debe ser 14, 16, 18, 20 o 25.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }

                if (puntadasInt == null || puntadasInt <= 0) {
                    Toast.makeText(
                        this,
                        "Introduce un número de puntadas válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                    puntadasGrafico.text.clear()
                    return@setOnClickListener
                }

                val madejas = calcularMadejas(
                    puntadasInt,
                    countTelaInt!!
                ) /* aserción no nula, kotlin sabe */

                /* crear y añadir el hilo a la tabla */
                val nuevoHilo = HiloGrafico(nombreHilo, madejas)
                grafico?.listaHilos?.add(nuevoHilo)

                /* ordenar la lista actualizada */
                val listaOrdenada = ordenarHilos(grafico?.listaHilos ?: emptyList()) { it.hilo }


                /* actualizar el gráfico y el adaptador con la lista ordenada */
                grafico?.listaHilos?.clear()
                grafico?.listaHilos?.addAll(listaOrdenada)
                adaptadorGrafico.actualizarLista(listaOrdenada)

                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al añadir el hilo: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /* borrar hilo del gráfico */
    private fun dialogBorrarHilo(hilo: HiloGrafico) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedidob_dialog_borrar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)

        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_deleteHilo)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_guardarHilo_dialog_deleteHilo)
        val txtMensaje = dialog.findViewById<TextView>(R.id.txtVw_textoInfo_dialog_deleteHilo)

        /* obtener texto original */
        val textoOriginal = getString(R.string.textoInfo_dialog_deleteHilo)

        /* reemplazarlo con el id del hilo */
        val textoConHilo = textoOriginal.replace("%s", hilo.hilo)

        /* teniendo el índice del hilo crear un spannable (aplica el estilo) */
        val spannable = SpannableString(textoConHilo)
        val start = textoConHilo.indexOf(hilo.hilo)
        val end = start + hilo.hilo.length

        if (start != -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        /* y se muestra en el dialog */
        txtMensaje.text = spannable
        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            /* se elimina de la lista y por tanto de la tabla */
            grafico?.listaHilos?.remove(hilo)

            /* TODO ordenar con la nueva función
            val listaOrdenada = ordenarHilos(grafico?.listaHilos ?: emptyList())
            adaptadorGrafico.actualizarLista(listaOrdenada)*/
            dialog.dismiss()
        }
        dialog.show()
    }

    /* volver a pedido_aa_principal mandando el número de madejas de cada gráfico */
    private fun devolverResultadoYSalir() {
        /* madejas de la lista hilos */
        grafico?.madejas = grafico?.listaHilos?.sumOf { it.cantidadModificar ?: it.madejas } ?: 0
        val resultIntent = Intent().apply {
            putExtra("grafico", grafico)
            putExtra("position", intent.getIntExtra("position", -1))
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }


}


