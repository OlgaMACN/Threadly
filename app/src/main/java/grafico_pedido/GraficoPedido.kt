package grafico_pedido

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
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

}

