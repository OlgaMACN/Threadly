package logica.almacen_pedidos

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import java.io.File
import java.io.FileWriter

class AlmacenPedidos : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        recyclerView = findViewById(R.id.tabla_almacen)

        adaptador = AdaptadorAlmacen(RepositorioPedidos.listaPedidos) { pedido ->
            val descargado = exportarPedidoACSV(pedido)
            val mensaje = if (descargado) "Pedido descargado en Descargas" else "Este pedido ya fue descargado"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adaptador

        buscadorPedido()
    }

    override fun onResume() {
        super.onResume()
        adaptador.actualizarLista(RepositorioPedidos.listaPedidos)
    }

    private fun buscadorPedido() {
        val edtBuscador = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val texto = edtBuscador.text.toString().trim().uppercase()
            val coincidencia = RepositorioPedidos.listaPedidos.find {
                it.nombre.uppercase().contains(texto)
            }

            if (coincidencia != null) {
                val listaFiltrada = listOf(coincidencia)
                adaptador.actualizarLista(listaFiltrada)
                txtNoResultados.visibility = View.GONE
            } else {
                adaptador.actualizarLista(emptyList())
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        edtBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptador.actualizarLista(RepositorioPedidos.listaPedidos)
                    txtNoResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun dialogEliminarPedido(posicion: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.almacen_dialog_eliminar_pedido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarPedido)
        val txtMensaje = dialog.findViewById<TextView>(R.id.txtVw_confirmarEliminarPedido)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_almacen)

        val pedido = RepositorioPedidos.listaPedidos[posicion]
        val nombrePedido = pedido.nombre
        val textoOriginal = getString(R.string.confirmarEliminarPedido)
        val textoConPedido = textoOriginal.replace("%s", nombrePedido)

        val spannable = SpannableString(textoConPedido)
        val start = textoConPedido.indexOf(nombrePedido)
        val end = start + nombrePedido.length

        if (start != -1) {
            spannable.setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        txtMensaje.text = spannable

        btnVolver.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            RepositorioPedidos.listaPedidos.removeAt(posicion)
            adaptador.actualizarLista(RepositorioPedidos.listaPedidos)
            Toast.makeText(this, "Pedido '$nombrePedido' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun exportarPedidoACSV(pedido: PedidoGuardado): Boolean {
        val nombreArchivo = "${pedido.nombre}.csv"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val archivo = File(downloadsDir, nombreArchivo)

        if (archivo.exists()) return false

        val mapaHilos = mutableMapOf<String, Int>()
        for (grafico in pedido.graficos) {
            for (hilo in grafico.listaHilos) {
                val codigo = hilo.hilo
                val cantidad = hilo.madejas
                mapaHilos[codigo] = mapaHilos.getOrDefault(codigo, 0) + cantidad
            }
        }

        return try {
            val writer = FileWriter(archivo)
            writer.append("Hilo,Madejas\n")
            for ((codigo, total) in mapaHilos) {
                writer.append("$codigo,$total\n")
            }
            writer.flush()
            writer.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
