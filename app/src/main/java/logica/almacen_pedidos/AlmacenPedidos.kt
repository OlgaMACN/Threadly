package logica.almacen_pedidos

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.funciones.exportarPedidoCSV
import utiles.funciones.funcionToolbar

class AlmacenPedidos : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        recyclerView = findViewById(R.id.tabla_almacen)

        adaptador = AdaptadorAlmacen(RepositorioPedidos.listaPedidos) { pedido ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val descargado = exportarPedidoCSV(this, pedido)
                val mensaje = if (descargado) "Pedido descargado en Documentos/Threadly" else "Este pedido ya fue descargado o ocurrió un error"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Esta función requiere Android 10 o superior", Toast.LENGTH_SHORT).show()
            }
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
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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
}
