package logica.almacen_pedidos

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import logica.pedido_hilos.PedidoHilos
import logica.stock_personal.StockSingleton
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.funciones.exportarPedidoCSV
import utiles.funciones.funcionToolbar

class AlmacenPedidos : BaseActivity() {

    private lateinit var tablaAlmacen: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        tablaAlmacen = findViewById(R.id.tabla_almacen)

        adaptador = AdaptadorAlmacen(
            PedidoSingleton.listaPedidos,
            onDescargarClick = { pedido ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resultado = exportarPedidoCSV(this, pedido)
                    if (resultado) {
                        Toast.makeText(
                            this,
                            "Pedido guardado en Descargas/Threadly",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "Error al descargar :(", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Exportación disponible solo en Android 10 o superior",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onEditarClick = { pedido ->  // Antes: onEditarClick
                dialogEditarPedido(pedido)
            },
            onPedidoRealizadoClick = { pedido ->
                pedido.graficos.forEach { grafico ->
                    grafico.listaHilos.forEach { hiloGrafico ->
                        // Suma las madejas pedidas a las madejas que ya tienes en stock
                        StockSingleton.agregarMadejas(hiloGrafico.hilo, hiloGrafico.madejas)
                    }
                }
                pedido.realizado = true
                Toast.makeText(this, "Pedido realizado y stock actualizado", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        tablaAlmacen.layoutManager = LinearLayoutManager(this)
        tablaAlmacen.adapter = adaptador
        buscadorPedido()
    }

    override fun onResume() {
        super.onResume()
        adaptador.actualizarLista(PedidoSingleton.listaPedidos)
    }

    private fun buscadorPedido() {
        val edtBuscador = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val texto = edtBuscador.text.toString().trim().uppercase()
            val coincidencia = PedidoSingleton.listaPedidos.find {
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
                    adaptador.actualizarLista(PedidoSingleton.listaPedidos)
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

        val pedido = PedidoSingleton.listaPedidos[posicion]
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
            PedidoSingleton.listaPedidos.removeAt(posicion)
            adaptador.actualizarLista(PedidoSingleton.listaPedidos)
            Toast.makeText(this, "Pedido '$nombrePedido' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun dialogEditarPedido(pedido: PedidoGuardado) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.almacen_dialog_editar_pedido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEditar = dialog.findViewById<Button>(R.id.btn_botonEditarPedido)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_almacen)
        val txtMensaje = dialog.findViewById<TextView>(R.id.txtVw_confirmarEditarPedido)

        val nombrePedido = pedido.nombre
        val textoOriginal = getString(R.string.confirmarEditarPedido)
        val textoConPedido = textoOriginal.replace("%s", nombrePedido)

        val spannable = SpannableString(textoConPedido)
        val start = textoConPedido.indexOf(nombrePedido)
        val end = start + nombrePedido.length

        if (start != -1) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        txtMensaje.text = spannable

        txtMensaje.text = textoConPedido
        btnEditar.setOnClickListener {
            val intent = Intent(this, PedidoHilos::class.java)
            intent.putExtra("pedido_a_editar", pedido)
            startActivity(intent)
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


}
