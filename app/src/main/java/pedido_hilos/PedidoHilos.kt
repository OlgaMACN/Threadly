package pedido_hilos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidoHilos : AppCompatActivity() {

    private lateinit var adaptadorpedidoA: AdaptadorPedido
    private val listaGraficos = mutableListOf<Grafico>()

    @SuppressLint("WrongViewCast")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)

        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)

        /* callback: pasa la función de eliminar gráfico directamente al adaptador, es decir, la tabla */
        adaptadorpedidoA = AdaptadorPedido(listaGraficos, ::dialogEliminarGrafico)
        tablaPedido.layoutManager = LinearLayoutManager(this)
        tablaPedido.adapter = adaptadorpedidoA

        /* declaración de botones */
        val btnAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)
        val btnDescargarPedido = findViewById<Button>(R.id.btn_descargarPedido)
        val btnRealizarPedido = findViewById<Button>(R.id.btn_realizarPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarGrafico.setOnClickListener { dialogAgregarGrafico() }
        btnDescargarPedido.setOnClickListener { descargarCSV() }
        btnRealizarPedido.setOnClickListener { realizarPedido() }
        buscadorGrafico()
    }

    @SuppressLint("SetTextI18n")
    private fun actualizarTotalMadejas() {
        val total = listaGraficos.sumOf { it.madejas }
        findViewById<TextView>(R.id.txtVw_madejasTotalPedido).text = "Total: $total"
    }

    @SuppressLint("SetTextI18n")
    fun buscadorGrafico() {
        val edTxtBuscador = findViewById<EditText>(R.id.edTxt_buscadorPedido)
        val btnBuscar = findViewById<ImageButton>(R.id.imgBtn_lupaPedido)
        val resultadoBusquedaContenedor =
            findViewById<LinearLayout>(R.id.contenedor_resultado_busqueda)
        val resultadoBusquedaTexto = findViewById<TextView>(R.id.txt_resultado_nombre)
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        val cabecera = findViewById<View>(R.id.cabecera_tabla_pedido)

        btnBuscar.setOnClickListener {
            val busqueda = edTxtBuscador.text.toString().trim()

            if (busqueda.isEmpty()) {
                /* si no hay nada escrito, la tabla vuelve a ser visible */
                resultadoBusquedaContenedor.visibility = View.GONE
                tablaPedido.visibility = View.VISIBLE
                cabecera.visibility = View.VISIBLE
                return@setOnClickListener
            }
            /* con it se representa cada elemento de la lista, es como el índice de un array */
            val coincidencia =
                listaGraficos.find { it.nombre.contains(busqueda, ignoreCase = true) }

            if (coincidencia != null) {
                resultadoBusquedaContenedor.visibility = View.VISIBLE
                tablaPedido.visibility = View.GONE
                cabecera.visibility = View.GONE
                resultadoBusquedaTexto.text = coincidencia.nombre

                resultadoBusquedaTexto.setOnClickListener {
                    val index = listaGraficos.indexOf(coincidencia)
                    tablaPedido.scrollToPosition(index)
                    resultadoBusquedaContenedor.visibility = View.GONE
                    tablaPedido.visibility = View.VISIBLE
                    cabecera.visibility = View.VISIBLE
                    edTxtBuscador.text.clear()
                }
            } else {
                Toast.makeText(this, "No tienes gráficos con ese nombre :(", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @SuppressLint("SetTextI18n") /* estas anotaciones surgen de ignorar las advertencias de lint para usar los recursos */
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

            if (nombre.isBlank() || countStr.isBlank()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else if (count == null) {
                Toast.makeText(this, "El count debe ser numérico", Toast.LENGTH_SHORT).show()
            } else {
                val nuevoGrafico =
                    Grafico(nombre, count, madejas = 0) /* placeholder, el valor se sabe luego */
                listaGraficos.add(nuevoGrafico)
                adaptadorpedidoA.notifyItemInserted(listaGraficos.size - 1)
                actualizarTotalMadejas()
                dialog.dismiss()
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
            listaGraficos.removeAt(index)
            adaptadorpedidoA.notifyItemRemoved(index)
            actualizarTotalMadejas()
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun descargarCSV() {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Pedido_$date.csv"
        val file = File(getExternalFilesDir(null), fileName)

        val contenido = StringBuilder()
        contenido.append("Nombre,Count Tela\n")
        listaGraficos.forEach {
            contenido.append("${it.nombre},${it.countTela}\n")
        }
        val totalMadejas = listaGraficos.sumOf { it.madejas }
        contenido.append("\nTotal Madejas,$totalMadejas")

        file.writeText(contenido.toString())

        Toast.makeText(this, "Archivo guardado: $fileName", Toast.LENGTH_LONG).show()
    }

    private fun realizarPedido() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_escoger_tienda)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<Button>(R.id.btn_amazon).setOnClickListener {
            abrirEnlace("https://www.amazon.es")
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btn_aliexpress).setOnClickListener {
            abrirEnlace("https://www.aliexpress.com")
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btn_temu).setOnClickListener {
            abrirEnlace("https://www.temu.com")
            dialog.dismiss()
        }

        dialog.show()
    }

    /* esta funcion es complementaria a realizar pedido, para abrir el enlace que el usuario escoja */
    private fun abrirEnlace(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        startActivity(intent)
    }
}