package grafico_pedido

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlin.math.ceil

class GraficoPedido : AppCompatActivity() {

    private lateinit var buscador: EditText
    private lateinit var totalMadejasView: TextView
    private lateinit var stockHiloView: TextView
    private lateinit var tablaGrafico: RecyclerView
    private lateinit var btnAgregarHilo: Button
    private lateinit var btnVolver: Button

    private var countTela: Int = 14 /* por defecto, para tener la variable, pero simpre va a llegar relleno */
    private var listaHilos = mutableListOf<HiloGrafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)

        /* llamada a la función para usar el toolbar */
        Toolbar.funcionToolbar(this)

        countTela = intent.getIntExtra("countTela",14)
        val index = intent.getIntExtra("graficoIndex", -1)

        buscador = findViewById(R.id.edTxt_buscadorGrafico)
        totalMadejasView = findViewById(R.id.txtVw_totalMadejasGraficoIndividual)
        stockHiloView = findViewById(R.id.txtVw_stockHiloActual)
        tablaGrafico = findViewById(R.id.tabla_grafico)
        btnAgregarHilo = findViewById(R.id.btn_agregarHiloGraficoIndividual)
        btnVolver = findViewById(R.id.btn_volver_pedido_desde_grafico)

        actualizarTotalMadejas()

        btnAgregarHilo.setOnClickListener {
            dialogAgregarHilo()
        }

        /* vuelve a la pantalla anterior, con el número de madejas */
        btnVolver.setOnClickListener {
            val total = listaHilos.sumOf { it.madejas }
            val resultIntent = Intent().apply {
                putExtra("totalMadejas", total)
                putExtra("graficoIndex", intent.getIntExtra("graficoIndex", -1))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
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
                listaHilos.add(nuevoHilo)
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
        val total = listaHilos.sumOf { it.madejas }
        totalMadejasView.text = total.toString()
    }

    private fun mostrarStockPersonalDeHilo(hilo: String) {
        val stock = obtenerStockHilo(hilo)
        stockHiloView.text = stock ?: "-"
    }

    private fun obtenerStockHilo(hilo: String): String? {
        /* todo necesito la BdD para poder coger datos */
        return TODO("Provide the return value")
    }
}

