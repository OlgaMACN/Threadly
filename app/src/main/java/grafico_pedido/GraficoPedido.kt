package grafico_pedido

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class GraficoPedido : AppCompatActivity() {


    private lateinit var adaptadorGrafico: AdaptadorGrafico
    private val listaHilosGrafico = mutableListOf<HiloGrafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)

        /* llamada a la función para usar el toolbar */
        toolbar.funcionToolbar(this)

        /* pasar el nombre del gráfico para ponerlo como cabecera del layout */
        val nombreGrafico = intent.getStringExtra("NOMBRE_GRAFICO")
        val cabecera: TextView = findViewById(R.id.txtVw_cabeceraGrafico)
        cabecera.text = nombreGrafico ?: "Gráfico"
    }
}

