package grafico_pedido

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.calcularMadejas
import utiles.funcionToolbar

class GraficoPedido : AppCompatActivity() {


    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private val listaHilosGrafico = mutableListOf<HiloGrafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* pasar el nombre del gráfico para ponerlo como cabecera del layout */
        val nombreGrafico = intent.getStringExtra("NOMBRE_GRAFICO")
        val cabecera: TextView = findViewById(R.id.txtVw_cabeceraGrafico)
        cabecera.text = nombreGrafico ?: "Gráfico"

        /* declaración de componentes */
        var buscador = findViewById<EditText>(R.id.edTxt_buscadorGrafico)

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

    fun mostrarDialogAgregarHilo(context: Context, listaHilosGrafico: MutableList<HiloGrafico>, adaptador: AdaptadorGrafico) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.pedidob_dialog_agregar_hilo, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        val edTxtHilo = dialogView.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val edTxtPuntadas = dialogView.findViewById<EditText>(R.id.edTxt_introducirPuntadas_dialog_addHilo)
        val edTxtCount = dialogView.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btn_guardar_dialog_pedidob_addHilo)
        val btnVolver = dialogView.findViewById<Button>(R.id.btn_volver_dialog_pedidob_addHilo)

        btnGuardar.setOnClickListener {
            val nombreHilo = edTxtHilo.text.toString().trim()
            val puntadasStr = edTxtPuntadas.text.toString().trim()
            val countTelaStr = edTxtCount.text.toString().trim()

            // Validaciones
            if (nombreHilo.isEmpty() || puntadasStr.isEmpty() || countTelaStr.isEmpty()) {
                Toast.makeText(context, "Por favor, rellena todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val puntadas = puntadasStr.toIntOrNull()
            val countTela = countTelaStr.toIntOrNull()

            if (puntadas == null || puntadas <= 0) {
                Toast.makeText(context, "Introduce un número válido de puntadas.", Toast.LENGTH_SHORT).show()
                edTxtPuntadas.text.clear()
                return@setOnClickListener
            }

            val countsPermitidos = listOf(14, 16, 18, 20, 25)
            if (countTela == null || countTela !in countsPermitidos) {
                Toast.makeText(context, "El count de tela debe ser 14, 16, 18, 20 o 25.", Toast.LENGTH_SHORT).show()
                edTxtCount.text.clear()
                return@setOnClickListener
            }

            // Cálculo de madejas
            val madejas = calcularMadejas(puntadas, countTela)

            // Crear hilo y añadirlo
            val nuevoHilo = HiloGrafico(
                hilo = nombreHilo,
                puntadas = puntadas,
                madejas = madejas
            )

            listaHilosGrafico.add(nuevoHilo)
            adaptador.actualizarLista(listaHilosGrafico)
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}

