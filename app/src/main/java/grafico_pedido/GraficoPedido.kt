package grafico_pedido

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private val listaHilosGrafico = mutableListOf<HiloGrafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* inicializar el adaptador y configurar el recycler view */
        val recyclerView = findViewById<RecyclerView>(R.id.tabla_grafico)
        adaptadorGrafico = AdaptadorGrafico(
            listaHilosGrafico,
            onClickHilo = { hilo ->
                Toast.makeText(this, "Hilo: $hilo", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adaptadorGrafico
        recyclerView.layoutManager = LinearLayoutManager(this)


        /* pasar el nombre del gráfico para ponerlo como cabecera del layout */
        val grafico = intent.getSerializableExtra("grafico") as? Grafico
        grafico?.let {
            val cabecera = findViewById<TextView>(R.id.txtVw_cabeceraGrafico)
            cabecera.text = it.nombre
        }

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
            val coincidencia = listaHilosGrafico.find { it.hilo.uppercase() == texto }

            if (coincidencia != null) {
                /* si encuentra el hilo lo resaltará en la tabla */
                adaptadorGrafico.resaltarHilo(coincidencia.hilo)
                adaptadorGrafico.actualizarLista(listaHilosGrafico)
                tablaGrafico.visibility = View.VISIBLE
                txtNoResultadosGrafico.visibility = View.GONE

                val index = listaHilosGrafico.indexOf(coincidencia)
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
                    adaptadorGrafico.resaltarHilo(null)
                    adaptadorGrafico.actualizarLista(listaHilosGrafico)
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
            val nombreHilo = hiloGrafico.text.toString().trim().uppercase()
            val stringPuntadas = puntadasGrafico.text.toString().trim()
            val stringCountTela = countTela.text.toString().trim()

            /* comprobar que los campos estén rellenos */
            if (nombreHilo.isEmpty() || stringPuntadas.isEmpty() || stringCountTela.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val puntadasInt = stringPuntadas.toIntOrNull()
            val countTelaInt = stringCountTela.toIntOrNull()

            if (puntadasInt == null || puntadasInt <= 0) {
                Toast.makeText(this, "Introduce un número de puntadas válido.", Toast.LENGTH_SHORT)
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
            val madejas = calcularMadejas(puntadasInt, countTelaInt)

            /* crear y añadir el hilo a la tabla */
            val nuevoHilo = HiloGrafico(nombreHilo, madejas)
            listaHilosGrafico.add(nuevoHilo)

            val listaOrdenada = ordenarHilos(listaHilosGrafico)
            listaHilosGrafico.clear()
            listaHilosGrafico.addAll(listaOrdenada)
            adaptadorGrafico.actualizarLista(listaOrdenada)

            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}

