package grafico_pedido_hilos

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.tuapp.R
import kotlin.math.ceil
class GraficoPedido : AppCompatActivity() {

    private lateinit var buscador: EditText
    private lateinit var totalMadejasView: TextView
    private lateinit var stockHiloView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregarHilo: Button
    private lateinit var btnVolver: Button

    private var countTela: Int = 14 // Este valor debería llegar desde el Intent o la tabla_pedido
    private var listaHilos = mutableListOf<HiloGrafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)

        buscador = findViewById(R.id.edTxt_buscadorGrafico)
        totalMadejasView = findViewById(R.id.txtVw_totalMadejasGraficoIndividual)
        stockHiloView = findViewById(R.id.txtVw_stockHiloActual)
        recyclerView = findViewById(R.id.tabla_grafico)
        btnAgregarHilo = findViewById(R.id.btn_agregarHiloGraficoIndividual)
        btnVolver = findViewById(R.id.btn_volver_pedido_desde_grafico)

        // Aquí puedes configurar el RecyclerView con tu adapter personalizado
        actualizarTotalMadejas()

        btnAgregarHilo.setOnClickListener {
            mostrarDialogAgregarHilo()
        }

        btnVolver.setOnClickListener {
            finish() // o pasa los datos de vuelta mediante Intent
        }
    }

    private fun mostrarDialogAgregarHilo() {
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

    // Puedes conectar este método con el clic en el campo "Hilo" de cada fila
    private fun mostrarStockPersonalDeHilo(hilo: String) {
        val stock = obtenerStockHilo(hilo)
        stockHiloView.text = stock ?: "-"
    }

    private fun obtenerStockHilo(hilo: String): String? {
        /* todo necesito la BdD para pulir esto */
        val stockFicticio = mapOf("310" to "3", "321" to "0", "666" to "2")
        return stockFicticio[hilo]
    }
}

