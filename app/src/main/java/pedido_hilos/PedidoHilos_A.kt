package pedido_hilos

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class PedidoHilos_A : AppCompatActivity() {

    private lateinit var adaptadorpedidoA: AdaptadorPedido_A
    private val listaGraficos = mutableListOf<Grafico>()
    private var totalMadejas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)

        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        val txtTotal = findViewById<TextView>(R.id.txtVw_madejasTotalPedido)

        /* callback: pasa la función de eliminar hilo directamente al adaptador, es decir, la tabla */
        adaptadorpedidoA = AdaptadorPedido_A(listaGraficos, ::dialogEliminarGrafico)
        tablaPedido.layoutManager = LinearLayoutManager(this)
        tablaPedido.adapter = adaptadorpedidoA

        /* declaración de botones */
        val botonAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)


        findViewById<Button>(R.id.btn_agregarGraficoPedido).setOnClickListener {
            dialogAgregarGrafico()
        }

        findViewById<Button>(R.id.btn_descargarPedido).setOnClickListener {
            descargarCSV()
        }

        findViewById<Button>(R.id.btn_realizarPedido).setOnClickListener {
            realizarPedido()
        }

        findViewById<EditText>(R.id.edTxt_buscadorPedido).addTextChangedListener {
            buscadorGrafico(it.toString())
        }
    }

    fun buscadorGrafico() {
        val buscador = findViewById<EditText>(R.id.edTxt_buscadorPedido)

        buscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val match = listaGraficos.find { it.nombre.contains(query, ignoreCase = true) }

                if (match != null) {
                    val index = listaGraficos.indexOf(match)
                    findViewById<RecyclerView>(R.id.tabla_pedido).scrollToPosition(index)
                } else if (query.isNotEmpty()) {
                    Toast.makeText(
                        this@PedidoHilos_A,
                        "No se encontraron gráficos con ese nombre",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    @SuppressLint("SetTextI18n")
    fun dialogAgregarGrafico() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_agregar_grafico)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        val nombreEdit = dialog.findViewById<EditText>(R.id.edTxt_pedirNombreGrafico)
        val countEdit = dialog.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarGrafico)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog_agregarGrafico)

        btnGuardar.setOnClickListener {
            val nombre = nombreEdit.text.toString().trim()
            val countStr = countEdit.text.toString().trim()
            val count = countStr.toIntOrNull()

            when {
                nombre.isBlank() || countStr.isBlank() -> {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                }
                count == null -> {
                    Toast.makeText(this, "El count debe ser numérico", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val grafico = Grafico(nombre, count)
                    listaGraficos.add(grafico)
                    totalMadejas += count
                    findViewById<TextView>(R.id.txtVw_madejasTotalPedido).text = "Total: $totalMadejas"
                    adaptadorpedidoA.notifyDataSetChanged()
                    dialog.dismiss()
                }
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    @SuppressLint("SetTextI18n")
    fun dialogEliminarGrafico(index: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_eliminar_grafico)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        val txtNombre = dialog.findViewById<TextView>(R.id.txtVw_nombreGrafico)
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_eliminarGrafico)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog_eliminarGrafico)

        txtNombre.text = listaGraficos[index].nombre

        btnEliminar.setOnClickListener {
            totalMadejas -= listaGraficos[index].countTela
            listaGraficos.removeAt(index)
            findViewById<TextView>(R.id.txtVw_madejasTotalPedido).text = "Total: $totalMadejas"
            // TODO cambiar esto por algo más eficiente
            adaptadorpedidoA.notifyDataSetChanged()
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun descargarCSV() {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Pedido_$date.csv"
        val file = File(getExternalFilesDir(null), fileName)

        val contenido = StringBuilder()
        contenido.append("Nombre,Count Tela\n")
        listaGraficos.forEach {
            contenido.append("${it.nombre},${it.countTela}\n")
        }
        contenido.append("\nTotal Madejas,$totalMadejas")

        file.writeText(contenido.toString())

        Toast.makeText(this, "Archivo guardado: $fileName", Toast.LENGTH_LONG).show()
    }

    fun realizarPedido() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://www.amazon.es") // puedes alternar con Aliexpress
        startActivity(intent)
    }

}