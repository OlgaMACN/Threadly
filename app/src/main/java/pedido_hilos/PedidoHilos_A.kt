package pedido_hilos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class PedidoHilos_A : AppCompatActivity() {

    private lateinit var adapter: AdaptadorPedido_A
    private val listaGraficos = mutableListOf<Grafico>()
    private var totalMadejas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)

        val recyclerView = findViewById<RecyclerView>(R.id.tabla_pedido)
        val txtTotal = findViewById<TextView>(R.id.txtVw_madejasTotalPedido)

        adapter = AdaptadorPedido_A(listaGraficos) { index ->
            mostrarDialogEliminar(index)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_agregarGraficoPedido).setOnClickListener {
            mostrarDialogAgregar()
        }

        findViewById<Button>(R.id.btn_descargarPedido).setOnClickListener {
            descargarCSV()
        }

        findViewById<Button>(R.id.btn_realizarPedido).setOnClickListener {
            compartirPedido()
        }

        findViewById<EditText>(R.id.edTxt_buscadorPedido).addTextChangedListener {
            filtrarGraficos(it.toString())
        }
    }
}