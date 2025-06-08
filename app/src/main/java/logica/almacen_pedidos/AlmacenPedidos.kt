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

/**
 * Actividad que muestra el almacén de pedidos guardados para el usuario.
 * Permite:
 *  - Listar todos los pedidos guardados.
 *  - Buscar pedidos por nombre.
 *  - Eliminar pedidos.
 *  - Descargar un pedido en CSV (Android Q+).
 *  - Marcar un pedido como realizado y actualizar el stock personal.
 *
 * Se conecta a Room mediante DAOs [PedidoDao], [HiloStockDao], [GraficoDao] y [HiloGraficoDao].
 * Utiliza [AdaptadorAlmacen] para renderizar la lista de [PedidoGuardado].
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class AlmacenPedidos : BaseActivity() {

    /** RecyclerView que muestra la lista de pedidos en el almacén */
    private lateinit var tablaAlmacen: RecyclerView

    /** Adaptador para renderizar y gestionar eventos sobre los pedidos */
    private lateinit var adaptador: AdaptadorAlmacen

    /** DAO para operaciones sobre la entidad PedidoEntity */
    private lateinit var pedidoDao: PedidoDao

    /** DAO para operaciones sobre HiloStockEntity (stock personal) */
    private lateinit var stockDao: HiloStockDao

    /** DAO para operaciones sobre GraficoEntity */
    private lateinit var graficoDao: GraficoDao

    /** DAO para operaciones sobre HiloGraficoEntity */
    private lateinit var hiloGraficoDao: HiloGraficoDao

    /** Lista en memoria de pedidos guardados mostrados en el adaptador */
    private val listaPedidos = mutableListOf<PedidoGuardado>()

    /** Identificador del usuario obtenido de sesión */
    private var userId: Int = -1

    /**
     * Punto de entrada de la actividad.
     * - Infla el layout `almacen_aa_pedidos`.
     * - Configura toolbar con [funcionToolbar].
     * - Obtiene `userId` de [SesionUsuario].
     * - Inicializa DAOs.
     * - Configura RecyclerView y [AdaptadorAlmacen] con callbacks:
     *   - Descargar CSV.
     *   - Marcar pedido como realizado y actualizar stock.
     * - Inicializa buscador y carga pedidos desde Room.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        /* obtener sesión de usuario */
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        /* inicializar DAOs */
        val db = ThreadlyDatabase.getDatabase(applicationContext)
        pedidoDao = db.pedidoDao()
        stockDao = db.hiloStockDao()
        graficoDao = db.graficoDao()
        hiloGraficoDao = db.hiloGraficoDao()

        /* configurar RecyclerView y adaptador */
        tablaAlmacen = findViewById(R.id.tabla_almacen)
        adaptador = AdaptadorAlmacen(
            listaPedidos,
            onDescargarClick = { pedido ->
                /* descargar CSV en Android */
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

    /**
     * Carga todos los pedidos del usuario desde Room.
     * Para cada [PedidoEntity]:
     *  - Obtiene sus [GraficoEntity].
     *  - Para cada gráfico, obtiene sus [HiloGraficoEntity] y los mapea a [HiloGrafico].
     *  - Construye [Grafico] y finalmente [PedidoGuardado].
     * Actualiza [listaPedidos] y notifica al adaptador.
     */
    private fun cargarPedidosDesdeRoom() {
        lifecycleScope.launch {
            val pedidosEnt = withContext(Dispatchers.IO) {
                pedidoDao.obtenerTodosPorUsuario(userId)
            }
            listaPedidos.clear()

            for (pedidoEnt in pedidosEnt) {
                val pedidoId = pedidoEnt.id
                val nombrePedido = pedidoEnt.nombre

                /* obtener gráficos asociados */
                val graficosEntidades = withContext(Dispatchers.IO) {
                    graficoDao.obtenerGraficoPorPedido(userId, pedidoId)
                }

                /* mapear cada gráfico a dominio */
                val listaGraficoDominio = mutableListOf<Grafico>()
                for (graficoEnt in graficosEntidades) {
                    val hilosEntidades = withContext(Dispatchers.IO) {
                        hiloGraficoDao.obtenerHilosDeGrafico(graficoEnt.id)
                    }
                    val listaHilosDominio = hilosEntidades.map { hiloEnt ->
                        HiloGrafico(hilo = hiloEnt.hilo, madejas = hiloEnt.madejas)
                    }.toMutableList()
                    listaGraficoDominio.add(
                        Grafico(nombre = graficoEnt.nombre, listaHilos = listaHilosDominio)
                    )
                }

                /* construir objeto PedidoGuardado */
                val pedidoDominio = PedidoGuardado(
                    id = pedidoId,
                    nombre = nombrePedido,
                    userId = pedidoEnt.userId,
                    realizado = pedidoEnt.realizado,
                    graficos = listaGraficoDominio
                )
                listaPedidos.add(pedidoDominio)
            }

            withContext(Dispatchers.Main) {
                adaptador.actualizarLista(listaPedidos)
            }
        }
    }

    /**
     * Configura el buscador de pedidos:
     * - Oculta teclado al buscar.
     * - Si el texto está vacío, restaura la lista completa.
     * - Filtra pedidos por nombre (insensible a mayúsculas), resalta y desplaza al primero.
     * - Muestra “sin resultados” si no hay coincidencias.
     */
    private fun buscadorAlmacen() {
        val buscarPedido = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)
        val tablaAlmacen = findViewById<RecyclerView>(R.id.tabla_almacen)
        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(buscarPedido.windowToken, 0)

            val texto = buscarPedido.text.toString().trim().uppercase()
            if (texto.isEmpty()) {
                /* restaurar lista completa */
                adaptador.actualizarLista(listaPedidos)
                adaptador.resaltarPedido(null)
                txtNoResultados.visibility = View.GONE
                return@setOnClickListener
            }

            /* filtrar coincidencias */
            val coincidencias = listaPedidos.filter {
                it.nombre.uppercase().contains(texto)
            }
            if (coincidencias.isNotEmpty()) {
                val primerMatch = coincidencias.first()
                val idx = listaPedidos.indexOf(primerMatch)
                adaptador.resaltarPedido(primerMatch.nombre)
                adaptador.actualizarLista(listaPedidos)
                tablaAlmacen.post { tablaAlmacen.scrollToPosition(idx) }
                txtNoResultados.visibility = View.GONE
            } else {
                /* sin resultados */
                adaptador.actualizarLista(emptyList())
                adaptador.resaltarPedido(null)
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        buscarPedido.addTextChangedListener(object : TextWatcher {
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

    /**
     * Muestra un diálogo de confirmación antes de eliminar un pedido:
     * - Resalta el nombre en rojo en el mensaje.
     * - Al confirmar, elimina del adaptador, muestra Toast y borra en Room.
     *
     * @param posicion Índice en [listaPedidos] del pedido a eliminar.
     */
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
        val textoOrig = getString(R.string.confirmarEliminarPedido)
        val textoCon = textoOrig.replace("%s", nombrePedido)
        val spannable = SpannableString(textoCon).apply {
            val inicio = textoCon.indexOf(nombrePedido)
            val fin = inicio + nombrePedido.length
            if (inicio != -1) {
                setSpan(
                    ForegroundColorSpan(Color.RED),
                    inicio,
                    fin,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        txtMensaje.text = spannable

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnEliminar.setOnClickListener {
            listaPedidos.removeAt(posicion)
            adaptador.actualizarLista(listaPedidos)
            Toast.makeText(this, "Pedido '$nombrePedido' eliminado correctamente", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            lifecycleScope.launch(Dispatchers.IO) {
                pedidoDao.eliminar(pedido.toEntity())
            }
        }
        dialog.show()
    }

    /**
     * Marca un pedido como realizado y actualiza el stock personal:
     * - Cambia `realizado` a true en DB.
     * - Recorre cada hilo de cada gráfico y suma madejas al stock (insert o update).
     * - Muestra un Toast y recarga la lista.
     *
     * @param pedido PedidoGuardado que se marca como realizado.
     */
    private fun marcarPedidoComoRealizadoYActualizarStock(pedido: PedidoGuardado) {
        lifecycleScope.launch(Dispatchers.IO) {
            /* marcar pedido */
            pedido.realizado = true
            pedidoDao.actualizar(pedido.toEntity())

            /* actualizar stock para cada hilo de cada gráfico */
            pedido.graficos.forEach { grafico ->
                grafico.listaHilos.forEach { hiloGrafico ->
                    val codigo = hiloGrafico.hilo
                    val nuevas = hiloGrafico.madejas
                    val hiloStock = stockDao.obtenerPorHiloUsuario(codigo, userId)
                    if (hiloStock == null) {
                        stockDao.insertarStock(
                            persistencia.entidades.HiloStockEntity(
                                usuarioId = userId,
                                hiloId = codigo,
                                madejas = nuevas
                            )
                        )
                    } else {
                        val actualizado = hiloStock.copy(madejas = hiloStock.madejas + nuevas)
                        stockDao.actualizarStock(actualizado)
                    }
                }
            }
            /* informar de que se ha guardado el pedido y se ha actualizado el stock */
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
