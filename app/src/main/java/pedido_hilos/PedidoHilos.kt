package pedido_hilos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class PedidoHilos : AppCompatActivity() {

    private lateinit var adaptadorPedido: AdaptadorPedido
    private val listaGraficos = mutableListOf<Grafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)

        /* llamada a la función para usar el toolbar */
        Toolbar.funcionToolbar(this)

        /* inicializar el adaptador */
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)

        /* inicializo el adaptador */
        val recyclerView = findViewById<RecyclerView>(R.id.tabla_pedido)
        adaptadorPedido = AdaptadorPedido(listaGraficos,
            onItemClick = { /* manejar click si quieres */ },
            onLongClick = { /* manejar long click si quieres */ }
        )
        recyclerView.adapter = adaptadorPedido
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* declarar componentes*/
        val btnAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarGrafico.setOnClickListener { dialogAgregarGrafico() }

        buscadorGrafico()

    }

    /* buscar un gráfico dentro del pedido */
    private fun buscadorGrafico() {
        val buscarPedido = findViewById<EditText>(R.id.edTxt_buscadorPedido)
        val btnLupaPedido = findViewById<ImageButton>(R.id.imgBtn_lupaPedido)
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        val txtNoResultadosPedido = findViewById<TextView>(R.id.txtVw_sinResultadosPedido)

        txtNoResultadosPedido.visibility = View.GONE

        btnLupaPedido.setOnClickListener {
            val graficoBuscado = buscarPedido.text.toString().trim().uppercase()
            val coincidencia = listaGraficos.find { it.nombre.uppercase() == graficoBuscado }

            if (coincidencia != null) {
                adaptadorPedido.resaltarGrafico(coincidencia.nombre)
                tablaPedido.visibility = View.VISIBLE
                txtNoResultadosPedido.visibility = View.GONE

                val index = listaGraficos.indexOf(coincidencia)
                tablaPedido.post {
                    tablaPedido.scrollToPosition(index)
                }
            } else {
                tablaPedido.visibility = View.GONE
                txtNoResultadosPedido.visibility = View.VISIBLE
            }
        }
        /* si se borra la búsqueda la tabla vuelve a aparecer */
        buscarPedido.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorPedido.resaltarGrafico(null)
                    adaptadorPedido.actualizarLista(listaGraficos)
                    tablaPedido.visibility = View.VISIBLE
                    txtNoResultadosPedido.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /* agregar un gráfico al pedido */
    private fun dialogAgregarGrafico() {
        val dialogView = layoutInflater.inflate(R.layout.pedido_dialog_agregar_grafico, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val nombreInput = dialogView.findViewById<EditText>(R.id.edTxt_pedirNombreGrafico)
        val countTelaInput = dialogView.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btn_guardarGrafico)
        val btnCancelar =
            dialogView.findViewById<Button>(R.id.btn_volver_pedido_dialog_agregarGrafico)

        btnGuardar.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val countTela = countTelaInput.text.toString().toIntOrNull()

            if (nombre.isEmpty() || countTela == null) {
                Toast.makeText(
                    this,
                    "Por favor completa todos los campos correctamente.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            /* verificar si ya existe un gráfico con ese nombre */
            if (listaGraficos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                Toast.makeText(this, "Ya existe un gráfico con ese nombre", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val nuevoGrafico = Grafico(nombre, countTela)
            listaGraficos.add(nuevoGrafico)
            listaGraficos.sortBy { it.nombre.lowercase() } /* para mostrar la tabla por orden alfabético */
            adaptadorPedido.actualizarLista(listaGraficos)

            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}