package grafico_pedido

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import pedido_hilos.Grafico
import ui_utils.ajustarDialog
import utiles.calcularMadejas
import utiles.funcionToolbar
import utiles.ordenarHilos

class GraficoPedido : AppCompatActivity() {


    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private var grafico: Grafico? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

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
            onClickHilo = { hilo -> Toast.makeText(this, "Hilo: $hilo", Toast.LENGTH_SHORT).show() }
        )
        recyclerView.adapter = adaptadorGrafico
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* declaración de componentes */
        var btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarHilo.setOnClickListener { dialogAgregarHiloGrafico() }
        /* inicio de las funciones en constante uso */
        buscadorHilo()

    }

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

        btnGuardar.setOnClickListener {
            try {
                val nombreHilo = hiloGrafico.text.toString().trim().uppercase()
                val stringPuntadas = puntadasGrafico.text.toString().trim()
                val stringCountTela = countTela.text.toString().trim()

                /* comprobar que los campos estén rellenos */
                if (nombreHilo.isEmpty() || stringPuntadas.isEmpty() || stringCountTela.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Por favor, completa todos los campos.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                val puntadasInt = try {
                    stringPuntadas.toInt()
                } catch (e: NumberFormatException) {
                    null
                }
                val countTelaInt = try {
                    stringCountTela.toInt()
                } catch (e: NumberFormatException) {
                    null
                }

                if (puntadasInt == null || puntadasInt <= 0) {
                    Toast.makeText(
                        this,
                        "Introduce un número de puntadas válido.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    puntadasGrafico.text.clear()
                    return@setOnClickListener
                }

                /* sólo counts permitidos */
                val countsPermitidos = listOf(14, 16, 18, 20, 25)
                if (countTelaInt == null || countTelaInt !in countsPermitidos) {
                    Toast.makeText(
                        this,
                        "El count de tela debe ser 14, 16, 18, 20 o 25.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                /* calcular madejas */
                val madejas = try {
                    calcularMadejas(puntadasInt, countTelaInt)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al calcular madejas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                /* crear y añadir el hilo a la tabla */
                val nuevoHilo = HiloGrafico(nombreHilo, madejas)

                grafico?.listaHilos?.add(nuevoHilo)

                val listaOrdenada = ordenarHilos(grafico?.listaHilos ?: emptyList())
                grafico?.listaHilos?.clear()
                grafico?.listaHilos?.addAll(listaOrdenada)
                adaptadorGrafico.actualizarLista(listaOrdenada)

                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}

