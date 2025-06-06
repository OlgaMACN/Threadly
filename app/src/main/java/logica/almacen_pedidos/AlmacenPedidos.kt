package logica.almacen_pedidos

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.grafico_pedido.HiloGrafico
import logica.pedido_hilos.Grafico
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.GraficoDao
import persistencia.daos.HiloGraficoDao
import persistencia.daos.HiloStockDao
import persistencia.daos.PedidoDao
import persistencia.entidades.GraficoEntity
import persistencia.entidades.HiloGraficoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog
import utiles.funciones.exportarPedidoCSV
import utiles.funciones.funcionToolbar

class AlmacenPedidos : BaseActivity() {

    private lateinit var tablaAlmacen: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen
    private lateinit var pedidoDao: PedidoDao
    private lateinit var stockDao: HiloStockDao
    private lateinit var graficoDao: GraficoDao
    private lateinit var hiloGraficoDao: HiloGraficoDao
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
        graficoDao = db.graficoDao()
        hiloGraficoDao = db.hiloGraficoDao()

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
            // 1) Leer todos los pedidos para este usuario
            val pedidosEnt = withContext(Dispatchers.IO) {
                pedidoDao.obtenerTodosPorUsuario(userId)
            }

            listaPedidos.clear()

            // 2) Por cada PedidoEntity, traemos sus gráficos y luego los hilos de cada gráfico
            for (pedidoEnt in pedidosEnt) {
                val pedidoId = pedidoEnt.id
                val nombrePedido = pedidoEnt.nombre

                // 2.2) Obtener todos los GraficoEntity que pertenecen a este pedido
                val graficosEntidades: List<GraficoEntity> = withContext(Dispatchers.IO) {
                    graficoDao.obtenerGraficoPorPedido(userId, pedidoId)
                }

                // 2.3) Para cada GraficoEntity, leemos sus hilos
                val listaGraficoDominio = mutableListOf<Grafico>()
                for (graficoEnt in graficosEntidades) {
                    // 2.3.1) Traer los HiloGraficoEntity de este grafico
                    val hilosEntidades: List<HiloGraficoEntity> = withContext(Dispatchers.IO) {
                        hiloGraficoDao.obtenerHilosDeGrafico(graficoEnt.id)
                    }

                    // 2.3.2) Mapear cada HiloGraficoEntity a tu modelo de dominio HiloGrafico
                    val listaHilosDominio = hilosEntidades.map { hiloEnt ->
                        HiloGrafico(
                            hilo = hiloEnt.hilo,
                            madejas = hiloEnt.madejas
                        )
                    }.toMutableList()

                    // 2.3.3) Construir el objeto Grafico (dominio) con su lista de hilos
                    listaGraficoDominio.add(
                        Grafico(
                            nombre = graficoEnt.nombre,
                            listaHilos = listaHilosDominio
                        )
                    )
                }

                // 2.4) Construir el objeto PedidoGuardado incluyendo userId
                val pedidoDominio = PedidoGuardado(
                    id = pedidoId,
                    nombre = nombrePedido,
                    userId = pedidoEnt.userId,
                    realizado = pedidoEnt.realizado,
                    graficos = listaGraficoDominio
                )

                listaPedidos.add(pedidoDominio)
            }

            // 3) Actualizar el adaptador en el hilo principal
            withContext(Dispatchers.Main) {
                adaptador.actualizarLista(listaPedidos)
            }
        }
    }

    private fun buscadorAlmacen() {
        val edtBuscador = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)
        val recycler = findViewById<RecyclerView>(R.id.tabla_almacen)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            // 1) Ocultamos el teclado antes de la búsqueda
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edtBuscador.windowToken, 0)

            // 2) Obtenemos el texto a buscar
            val texto = edtBuscador.text.toString().trim().uppercase()

            // 3) Si está vacío, restauramos
            if (texto.isEmpty()) {
                adaptador.actualizarLista(listaPedidos)
                adaptador.resaltarPedido(null)
                txtNoResultados.visibility = View.GONE
                return@setOnClickListener
            }

            // 4) Filtrar coincidencias
            val coincidencias = listaPedidos.filter {
                it.nombre.uppercase().contains(texto)
            }

            if (coincidencias.isNotEmpty()) {
                // Tomamos la primera (la más antigua)
                val primerMatch = coincidencias.first()
                val indiceEnListaCompleta = listaPedidos.indexOf(primerMatch)

                // Resaltamos y desplazamos
                adaptador.resaltarPedido(primerMatch.nombre)
                adaptador.actualizarLista(listaPedidos)
                recycler.post {
                    recycler.scrollToPosition(indiceEnListaCompleta)
                }
                txtNoResultados.visibility = View.GONE
            } else {
                // Si no hay coincidencias, vaciamos y mostramos “sin resultados”
                adaptador.actualizarLista(emptyList())
                adaptador.resaltarPedido(null)
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        edtBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
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
     * Marca el pedido como realizado en la base de datos y, a la vez,
     * añade las madejas de cada hilo al stock personal del usuario.
     */
    private fun marcarPedidoComoRealizadoYActualizarStock(pedido: PedidoGuardado) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1) Marcar el pedido como realizado
            pedido.realizado = true
            pedidoDao.actualizar(pedido.toEntity())

            // 2) Recorrer cada hilo de cada gráfico y sumar madejas al stock
            pedido.graficos.forEach { grafico ->
                grafico.listaHilos.forEach { hiloGrafico ->
                    val codigoHilo = hiloGrafico.hilo
                    val madejasNuevas = hiloGrafico.madejas

                    // 2a) Intentamos obtener la entidad HiloStockEntity para este usuario+hilo
                    val entidadExistente = stockDao.obtenerPorHiloUsuario(codigoHilo, userId)

                    if (entidadExistente == null) {
                        // No existía stock → insertamos uno nuevo
                        val nuevaEntidad = persistencia.entidades.HiloStockEntity(
                            usuarioId = userId,
                            hiloId = codigoHilo,
                            madejas = madejasNuevas
                        )
                        stockDao.insertarStock(nuevaEntidad)
                    } else {
                        // Ya existe: sumamos madejas y actualizamos
                        val acumulado = entidadExistente.madejas + madejasNuevas
                        val entidadActualizada = entidadExistente.copy(madejas = acumulado)
                        stockDao.actualizarStock(entidadActualizada)
                    }
                }
            }

            // 3) Volver al hilo principal: toast y recargar la lista
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
