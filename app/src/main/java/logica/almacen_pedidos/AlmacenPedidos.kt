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
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.PedidoGuardado
import modelo.toEntity
import modelo.toPedidoGuardado
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.PedidoDao
import persistencia.entidades.PedidoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog
import utiles.funciones.exportarPedidoCSV
import utiles.funciones.funcionToolbar

class AlmacenPedidos : BaseActivity() {

    private lateinit var tablaAlmacen: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen
    private lateinit var pedidoDao: PedidoDao
    private val listaPedidos = mutableListOf<PedidoGuardado>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        // Inicializamos DAO
        val db = ThreadlyDatabase.getDatabase(applicationContext)
        pedidoDao = db.pedidoDao()

        tablaAlmacen = findViewById(R.id.tabla_almacen)

        adaptador = AdaptadorAlmacen(
            listaPedidos,
            onDescargarClick = { pedido ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resultado = exportarPedidoCSV(this, pedido)
                    Toast.makeText(
                        this,
                        if (resultado) "Pedido guardado en Descargas/Threadly" else "Error al descargar :(",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Exportación disponible solo en Android 10 o superior",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onPedidoRealizadoClick = { pedido ->
                // Actualizar stock si es necesario…
                pedido.realizado = true
                lifecycleScope.launch(Dispatchers.IO) {
                    pedidoDao.actualizar(pedido.toEntity())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AlmacenPedidos,
                            "Pedido realizado y stock actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        tablaAlmacen.layoutManager = LinearLayoutManager(this)
        tablaAlmacen.adapter = adaptador

        buscadorPedido()
        cargarPedidosDesdeRoom()
    }

    override fun onResume() {
        super.onResume()
        cargarPedidosDesdeRoom()
    }

    private fun cargarPedidosDesdeRoom() {
        val userId = SesionUsuario.obtenerSesion(this)
        lifecycleScope.launch {
            val pedidosEnt = withContext(Dispatchers.IO) {
                pedidoDao.obtenerTodosPorUsuario(userId)
            }
            listaPedidos.clear()
            listaPedidos.addAll(pedidosEnt.map { it.toPedidoGuardado() })
            adaptador.actualizarLista(listaPedidos)
            Log.d("AlmacenPedidos", "Pedidos cargados: ${listaPedidos.size}")
        }
    }

    private fun buscadorPedido() {
        val edtBuscador = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)

        txtNoResultados.visibility = TextView.GONE

        btnLupa.setOnClickListener {
            val texto = edtBuscador.text.toString().trim().uppercase()
            if (texto.isEmpty()) {
                adaptador.actualizarLista(listaPedidos)
                txtNoResultados.visibility = TextView.GONE
                return@setOnClickListener
            }
            val listaFiltrada = listaPedidos.filter {
                it.nombre.uppercase().contains(texto)
            }
            if (listaFiltrada.isNotEmpty()) {
                adaptador.actualizarLista(listaFiltrada)
                txtNoResultados.visibility = TextView.GONE
            } else {
                adaptador.actualizarLista(emptyList())
                txtNoResultados.visibility = TextView.VISIBLE
            }
        }

        edtBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptador.actualizarLista(listaPedidos)
                    txtNoResultados.visibility = TextView.GONE
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

        val pedido = listaPedidos[posicion]
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
            listaPedidos.removeAt(posicion)
            adaptador.actualizarLista(listaPedidos)
            Toast.makeText(this, "Pedido '$nombrePedido' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            lifecycleScope.launch(Dispatchers.IO) {
                pedidoDao.eliminar(pedido.toEntity())
            }
        }

        dialog.show()
    }
}
