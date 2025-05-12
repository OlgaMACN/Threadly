package grafico_pedido

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlin.math.ceil

class GraficoPedido : AppCompatActivity() {

    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private val listaHilosGrafico = mutableListOf<HiloGrafico>()

    /* por defecto, para tener la variable, pero siempre va a llegar relleno de PedidoHilos.kt */
    private var countTela: Int = 14

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)

        /* llamada a la función para usar el toolbar */
        Toolbar.funcionToolbar(this)

        /* entrada de los datos de la otra pantalla, recogidos */
        countTela = intent.getIntExtra("countTela", 14)
        val index = intent.getIntExtra("graficoIndex", -1)

        /* declaración de componenentes */
        var buscador = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        var totalMadejasView = findViewById<TextView>(R.id.txtVw_totalMadejasGraficoIndividual)
        var stockHiloView = findViewById<TextView>(R.id.txtVw_stockHiloActual)
        var tablaGrafico = findViewById<RecyclerView>(R.id.tabla_grafico)
        val btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual)
        val btnVolver = findViewById<Button>(R.id.btn_volver_pedido_desde_grafico)

        /* inicio de las funciones en constante uso */
        buscadorHilo()
        actualizarTotalMadejas()

        btnAgregarHilo.setOnClickListener { dialogAgregarHilo() }
        /* vuelve a la pantalla anterior, llevándose consigo el número de madejas del gráfico */
        btnVolver.setOnClickListener {
            val total = listaHilosGrafico.sumOf { it.madejas }
            val resultIntent = Intent().apply {
                putExtra("totalMadejas", total)
                putExtra("graficoIndex", intent.getIntExtra("graficoIndex", -1))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
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

    private fun dialogAgregarHilo() {
        val dialogView = layoutInflater.inflate(R.layout.pedidob_dialog_agregar_hilo, null)
        val hiloInput = dialogView.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val puntadasInput =
            dialogView.findViewById<EditText>(R.id.edTxt_introducirPuntadas_dialog_addHilo)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_guardar_dialog_pedidob_addHilo)
            .setOnClickListener {
                val hilo = hiloInput.text.toString().trim()
                val puntadasStr = puntadasInput.text.toString().trim()

                if (hilo.isEmpty() || puntadasStr.isEmpty()) {
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val puntadas = puntadasStr.toIntOrNull()
                if (puntadas == null || puntadas <= 0) {
                    Toast.makeText(this, "Puntadas inválidas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val madejas = calcularMadejas(puntadas, countTela)

                val nuevoHilo = HiloGrafico(hilo, puntadas, madejas)
                listaHilosGrafico.add(nuevoHilo)
                // recyclerView.adapter?.notifyDataSetChanged()
                actualizarTotalMadejas()
                dialog.dismiss()
            }

        dialogView.findViewById<Button>(R.id.btn_volver_dialog_pedidob_addHilo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun calcularMadejas(puntadas: Int, countTela: Int): Int {
        // En esta versión, siempre se calcula con 2 hebras
        val madejas = puntadas / 1700.0
        return ceil(madejas).toInt()
    }

    private fun actualizarTotalMadejas() {
        /* val total = listaHilosGrafico.sumOf { it.madejas }
         totalMadejasView.text = total.toString()*/
    }

    private fun mostrarStockPersonalDeHilo(hilo: String) {
       /* val stock = obtenerStockHilo(hilo)
        stockHiloView.text = stock ?: "-"*/
    }

    private fun obtenerStockHilo(hilo: String): String? {
        /* todo necesito la BdD para poder coger datos */
        return TODO("Provide the return value")
    }
}

