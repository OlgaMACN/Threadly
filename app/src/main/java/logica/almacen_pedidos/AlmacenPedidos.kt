package logica.almacen_pedidos

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.PedidoGuardado
import modelo.toEntity
import modelo.toPedidoGuardado
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloStockDao
import persistencia.daos.PedidoDao
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog
import utiles.funciones.exportarPedidoCSV
import utiles.funciones.funcionToolbar
import java.io.File
import java.io.FileWriter

class AlmacenPedidos : BaseActivity() {

    private lateinit var tablaAlmacen: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen
    private lateinit var pedidoDao: PedidoDao
    private lateinit var stockDao: HiloStockDao
    private val listaPedidos = mutableListOf<PedidoGuardado>()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        // Obtenemos el userId de la sesión
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        // 1) Inicializar DAOs
        val db = ThreadlyDatabase.getDatabase(applicationContext)
        pedidoDao = db.pedidoDao()
        stockDao = db.hiloStockDao()

        // 2) Configurar RecyclerView y Adaptador
        tablaAlmacen = findViewById(R.id.tabla_almacen)
        adaptador = AdaptadorAlmacen(
            listaPedidos,
            onDescargarClick = { pedido ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val ok = exportarPedidoCSV(this, pedido)
                    Toast.makeText(
                        this,
                        if (ok) "Pedido guardado en Descargas/Threadly" else "Error al descargar :(",
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
                marcarPedidoComoRealizadoYActualizarStock(pedido)
            },
            onNombrePedidoClick = { pedido ->
                mostrarCsvEnVista(pedido)
            }
        )
        tablaAlmacen.layoutManager = LinearLayoutManager(this)
        tablaAlmacen.adapter = adaptador

        buscadorAlmacen()
        cargarPedidosDesdeRoom()
    }

    override fun onResume() {
        super.onResume()
        cargarPedidosDesdeRoom()
    }

    private fun cargarPedidosDesdeRoom() {
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

    private fun buscadorAlmacen() {
        val edtBuscador = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)
        val recycler = findViewById<RecyclerView>(R.id.tabla_almacen) // ajustar id real

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            // 1) Ocultamos el teclado antes de la búsqueda
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edtBuscador.windowToken, 0)

            // 1) Obtenemos el texto a buscar
            val texto = edtBuscador.text.toString().trim().uppercase()

            // 2) Si el usuario borró todo, restablecemos
            if (texto.isEmpty()) {
                adaptador.actualizarLista(listaPedidos)
                adaptador.resaltarPedido(null)
                txtNoResultados.visibility = View.GONE
                return@setOnClickListener
            }

            // 3) Buscamos todas las coincidencias (contains)
            val coincidencias = listaPedidos.filter {
                it.nombre.uppercase().contains(texto)
            }

            if (coincidencias.isNotEmpty()) {
                // 4) Tomamos la primera (será la más antigua si listaPedidos ya está ordenada)
                val primerMatch = coincidencias.first()
                val indiceEnListaCompleta = listaPedidos.indexOf(primerMatch)

                // 5) Resaltamos ese nombre en el adaptador y nos desplazamos a dicha posición
                adaptador.resaltarPedido(primerMatch.nombre)
                adaptador.actualizarLista(listaPedidos) // restauramos lista completa
                recycler.post {
                    recycler.scrollToPosition(indiceEnListaCompleta)
                }
                txtNoResultados.visibility = View.GONE
            } else {
                // 6) No hay coincidencias: vaciamos el adaptador y mostramos “sin resultados”
                adaptador.actualizarLista(emptyList())
                adaptador.resaltarPedido(null)
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        edtBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    // Al borrar todo, volvemos a mostrar lista completa sin resaltado
                    adaptador.actualizarLista(listaPedidos)
                    adaptador.resaltarPedido(null)
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

    /**
     * Genera un CSV temporal y lo abre con un lector de CSV instalado.
     */
    private fun mostrarCsvEnVista(pedido: PedidoGuardado) {
        lifecycleScope.launch {
            // 1) Agrupamos madejas por hilo
            val agregados = mutableMapOf<String, Int>()
            pedido.graficos.forEach { grafico ->
                grafico.listaHilos.forEach { hiloGrafico ->
                    val prev = agregados[hiloGrafico.hilo] ?: 0
                    agregados[hiloGrafico.hilo] = prev + hiloGrafico.madejas
                }
            }

            // 2) Creamos archivo CSV en cacheDir
            val nombreArchivo = "pedido_${pedido.id}.csv"
            val archivo = File(cacheDir, nombreArchivo)
            FileWriter(archivo).use { writer ->
                writer.append("Hilo,Madejas\n")
                agregados.forEach { (hilo, total) ->
                    writer.append("$hilo,$total\n")
                }
            }

            // 3) URI a través de FileProvider
            val authority = "${packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(
                this@AlmacenPedidos,
                authority,
                archivo
            )

            // 4) Intent ACTION_VIEW con MIME "text/csv"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // 5) Verificar que exista app capaz de abrir CSV
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Abrir con"))
            } else {
                Toast.makeText(
                    this@AlmacenPedidos,
                    "No se encontró ninguna aplicación para ver archivos CSV.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Marca el pedido como realizado en la base de datos y, a la vez,
     * añade las madejas de cada hilo al stock personal del usuario.
     */
    private fun marcarPedidoComoRealizadoYActualizarStock(pedido: PedidoGuardado) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1) Marcar el pedido como realizado en la tabla 'pedidos'
            pedido.realizado = true
            pedidoDao.actualizar(pedido.toEntity())

            // 2) Para cada gráfico y cada hilo en ese pedido,
            //    sumamos madejas al stock personal (insertar o actualizar).
            pedido.graficos.forEach { grafico ->
                grafico.listaHilos.forEach { hiloGrafico ->
                    val codigoHilo = hiloGrafico.hilo
                    val madejasNuevo = hiloGrafico.madejas

                    // Obtenemos las madejas actuales (si existen) para este usuario y ese hilo
                    val actual = stockDao.obtenerMadejas(userId, codigoHilo)

                    if (actual == null) {
                        // No existe registro => insertamos uno nuevo
                        val nuevaEntidad = persistencia.entidades.HiloStockEntity(
                            usuarioId = userId,
                            hiloId = codigoHilo,
                            madejas = madejasNuevo
                        )
                        stockDao.insertarStock(nuevaEntidad)
                    } else {
                        // Ya existe => actualizamos sumando las madejas
                        val acumulado = actual + madejasNuevo
                        stockDao.actualizarMadejas(userId, codigoHilo, acumulado)
                    }
                }
            }

            // 3) De vuelta al hilo principal, mostramos Toast y recargamos la lista de pedidos
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AlmacenPedidos,
                    "Pedido marcado como realizado y stock actualizado",
                    Toast.LENGTH_SHORT
                ).show()
                cargarPedidosDesdeRoom()
            }
        }
    }

}
