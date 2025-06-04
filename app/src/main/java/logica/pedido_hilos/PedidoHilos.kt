package logica.pedido_hilos

import android.app.Dialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.grafico_pedido.GraficoPedido
import logica.grafico_pedido.HiloGrafico
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.GraficoDao
import persistencia.daos.HiloGraficoDao
import persistencia.daos.PedidoDao
import persistencia.entidades.HiloGraficoEntity
import persistencia.entidades.PedidoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import java.util.Date
import java.util.Locale

/**
 * Clase principal para la pantalla de creación y edición de pedidos en Threadly.
 *
 * Permite al usuario agregar gráficos a un pedido, editar sus hilos, buscar gráficos,
 * eliminar gráficos del pedido, guardar el pedido actual o realizar el pedido en tiendas externas.
 *
 * La pantalla se adapta para edición si se recibe un pedido ya guardado como extra.
 *
 * @author Olga y Sandra Macías Aragón
 */

private val REQUEST_CODE_GRAFICO_PEDIDO = 1 /* para identificar cada gráfico */

class PedidoHilos : BaseActivity() {

    private lateinit var adaptadorPedido: AdaptadorPedido
    private var listaGraficos: MutableList<Grafico> = mutableListOf()
    private var pedidoGuardado = false
    private var nombrePedidoEditado: String? = null
    private lateinit var btnGuardarPedido: Button
    private var userId: Int = -1


    // DAOs
    private lateinit var graficoDao: GraficoDao
    private lateinit var hiloGraficoDao: HiloGraficoDao
    private lateinit var pedidoDao: PedidoDao

    /**
     * Método principal al crear la actividad. Inicializa la vista, carga un pedido si se va a editar
     * y configura los listeners y elementos visuales.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        // 1) Instanciamos los DAOs
        graficoDao = ThreadlyDatabase.getDatabase(applicationContext).graficoDao()
        hiloGraficoDao = ThreadlyDatabase.getDatabase(applicationContext).hiloGraficoDao()
        pedidoDao = ThreadlyDatabase.getDatabase(applicationContext).pedidoDao()

        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        // 2) Inicializamos RecyclerView y Adaptador
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)
        adaptadorPedido = AdaptadorPedido(
            listaGraficos,
            onItemClick = { graficoSeleccionado ->
                val index = listaGraficos.indexOf(graficoSeleccionado)
                lanzarConResultado(GraficoPedido::class.java, REQUEST_CODE_GRAFICO_PEDIDO) {
                    putExtra("grafico", graficoSeleccionado)
                    putExtra("position", index)
                }
            },
            onEliminarGrafico = { index ->
                dialogoEliminarGrafico(index)
            }
        )
        tablaPedido.adapter = adaptadorPedido


        /* declarar componentes*/
        btnGuardarPedido = findViewById(R.id.btn_guardarPedidoA)
        btnGuardarPedido.isEnabled = true
        btnGuardarPedido.setOnClickListener { mostrarDialogoConfirmarGuardado() }


        val btnAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)
        val btnRealizarPedido = findViewById<Button>(R.id.btn_realizarPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarGrafico.setOnClickListener { dialogAgregarGrafico() }
        btnRealizarPedido.setOnClickListener { realizarPedido() }


        /* funciones en continua ejecución durante la pantalla */
        cargarGraficosTemporalesEnMemoria()
        buscadorGrafico()
        actualizarTotalMadejas()
    }

    /**
     * 3) Carga desde Room todos los GraficoEntity con idPedido = NULL para este userId.
     *    Para cada uno, además, carga sus HiloGraficoEntity y lo transforma a dominio.
     */
    private fun cargarGraficosTemporalesEnMemoria() {
        lifecycleScope.launch {
            val entidadesGrafico = withContext(Dispatchers.IO) {
                graficoDao.obtenerGraficosEnCurso(userId)
            }

            // Para cada GraficoEntity, necesitamos leer sus HiloGraficoEntity y mapearlos a HiloGrafico
            listaGraficos.clear()
            for (graficoEntity in entidadesGrafico) {
                val hilosEntidades = withContext(Dispatchers.IO) {
                    hiloGraficoDao.obtenerHilosDeGrafico(graficoEntity.id)
                }
                // Mapeamos a dominio: Grafico(nombre, listaHilos=MutableList<HiloGrafico>)
                val listaHilosDominio = hilosEntidades.map { ent ->
                    HiloGrafico(ent.hilo, ent.madejas)
                }.toMutableList()

                listaGraficos.add(
                    Grafico(
                        nombre = graficoEntity.nombre,
                        listaHilos = listaHilosDominio
                    )
                )
            }

            // Ordenar alfabéticamente (o como prefieras)
            listaGraficos.sortBy { it.nombre.lowercase() }
            adaptadorPedido.actualizarLista(listaGraficos)
            actualizarTotalMadejas()
            validarBotonGuardar()
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de guardar el pedido actual.
     * Si el usuario confirma, guarda el pedido y lo elimina de memoria.
     */
    private fun mostrarDialogoConfirmarGuardado() {
        if (listaGraficos.isEmpty()) {
            Toast.makeText(this, "El pedido está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_guardar_pedido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_guardarPedido)
        val btnCancelar = dialog.findViewById<Button>(R.id.btn_volverSinGuardar)

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            guardarPedidoEnBD()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validarBotonGuardar() {
        val habilitado = listaGraficos.isNotEmpty()
        btnGuardarPedido.alpha = if (habilitado) 1.0f else 0.5f
    }

    /**
     * Muestra un buscador para filtrar gráficos por nombre.
     * Permite al usuario escribir y resaltar coincidencias.
     */
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
                    actualizarTotalMadejas()
                    tablaPedido.visibility = View.VISIBLE
                    txtNoResultadosPedido.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Actualiza el total de madejas mostradas en la interfaz.
     */
    private fun actualizarTotalMadejas() {
        val txtTotal = findViewById<TextView>(R.id.txtVw_madejasTotalPedido)
        val total = listaGraficos.sumOf { grafico ->
            grafico.listaHilos.sumOf { hiloGrafico -> hiloGrafico.madejas }
        }
        txtTotal.text = "Total madejas: $total"
    }

    /**
     * Muestra un diálogo para agregar un nuevo gráfico al pedido.
     * Valida que no haya duplicados y que el nombre no esté vacío.
     */
    private fun dialogAgregarGrafico() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_agregar_grafico)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val nombreInput = dialog.findViewById<EditText>(R.id.edTxt_pedirNombreGrafico)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarGrafico)
        val btnCancelar = dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog_agregarGrafico)

        btnGuardar.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce un nombre.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (listaGraficos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                Toast.makeText(this, "Ya existe un gráfico con ese nombre", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // ① Insertar el GraficoEntity en Room, aunque no tenga hilos todavía:
            lifecycleScope.launch(Dispatchers.IO) {
                graficoDao.insertarGrafico(
                    persistencia.entidades.GraficoEntity(
                        nombre = nombre,
                        idPedido = null,
                        userId = userId
                    )
                )
            }

            // ② En paralelo, añadimos al listado en memoria el dominio Grafico con listaHilos vacía:
            val nuevoGrafico = Grafico(
                nombre = nombre,
                listaHilos = mutableListOf()
            )
            listaGraficos.add(nuevoGrafico)
            listaGraficos.sortBy { it.nombre.lowercase() }
            adaptadorPedido.actualizarLista(listaGraficos)
            actualizarTotalMadejas()
            validarBotonGuardar()
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    /**
     * Muestra un diálogo de confirmación para eliminar un gráfico del pedido.
     *
     * @param index Índice del gráfico en la lista.
     */
    /**
     * 4b) Mostrar diálogo de confirmación para eliminar un gráfico de la lista (tanto en memoria como en DB).
     */
    private fun dialogoEliminarGrafico(index: Int) {
        val graficoDom = listaGraficos[index]
        val dialog = Dialog(this).apply {
            setContentView(R.layout.pedido_dialog_eliminar_grafico)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val txtNombreGrafico = dialog.findViewById<TextView>(R.id.txtVw_nombreGrafico)
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_eliminarGrafico)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog)

        txtNombreGrafico.text = graficoDom.nombre

        btnEliminar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                // 1) Primero obtenemos el id de GraficoEntity por nombre (solo temporales)
                val entidad = graficoDao.obtenerGraficoEnCursoPorNombre(userId, graficoDom.nombre)
                if (entidad != null) {
                    // 2) Borrar todos sus HiloGraficoEntity relacionados
                    val hilosEnt = hiloGraficoDao.obtenerHilosDeGrafico(entidad.id)
                    for (h in hilosEnt) {
                        hiloGraficoDao.eliminarHiloDeGrafico(h)
                    }
                    // 3) Borrar el propio grafico
                    graficoDao.eliminarGraficoEnCurso(entidad.id)
                }
                // 4) Actualizar la UI en el hilo principal
                withContext(Dispatchers.Main) {
                    listaGraficos.removeAt(index)
                    adaptadorPedido.actualizarLista(listaGraficos)
                    actualizarTotalMadejas()
                    validarBotonGuardar()
                    dialog.dismiss()
                }
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Muestra un diálogo con opciones para realizar el pedido en tiendas externas (Amazon, AliExpress, Temu).
     */
    private fun realizarPedido() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_realizar_pedido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        /* variables del dialog */
        val btnAmazon = dialog.findViewById<ImageButton>(R.id.btn_amazon)
        val btnAliExpress = dialog.findViewById<ImageButton>(R.id.btn_aliexpress)
        val btnTemu = dialog.findViewById<ImageButton>(R.id.btn_temu)
        val btnVolver =
            dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog)

        btnAmazon.setOnClickListener {
            abrirTienda("com.amazon.mShop.android.shopping", "https://www.amazon.es/")
            dialog.dismiss()
        }

        btnAliExpress.setOnClickListener {
            abrirTienda("com.alibaba.aliexpresshd", "https://www.aliexpress.com/")
            dialog.dismiss()
        }

        btnTemu.setOnClickListener {
            abrirTienda("com.einnovation.temu", "https://www.temu.com/")
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Lanza la app de una tienda si está instalada, o su web si no lo está.
     *
     * @param paquete Nombre del paquete de la aplicación.
     * @param urlWeb URL de la tienda online.
     */
    private fun abrirTienda(paquete: String, urlWeb: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(paquete)
            if (intent != null) {
                startActivity(intent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWeb))
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al redirigir a la tienda.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 5a) Aquí persistimos en Room el Pedido completo:
     *     1) Creamos PedidoEntity y lo insertamos.
     *     2) Llamamos a graficoDao.asociarGraficosAlPedido para “archivar” todos los gráficos con idPedido = null.
     *     3) Limpiamos la lista en memoria y la interfaz.
     */
    private fun guardarPedidoEnBD() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1) Generar un nombre único para el pedido
            val nombreFinal = nombrePedidoUnico()
            // 2) Insertar en la tabla "pedidos"
            val nuevoPedidoId = pedidoDao.insertarPedido(
                PedidoEntity(
                    nombre = nombreFinal,
                    userId = userId
                )
            ).toInt()

            // 3) Asociar todos los gráficos “en curso” (idPedido = null) de este userId al nuevoPedidoId
            graficoDao.asociarGraficosAlPedido(userId, nuevoPedidoId)

            // En el hilo principal, limpiar la UI
            withContext(Dispatchers.Main) {
                listaGraficos.clear()
                adaptadorPedido.actualizarLista(listaGraficos)
                actualizarTotalMadejas()
                validarBotonGuardar()
                Toast.makeText(
                    this@PedidoHilos,
                    "Pedido guardado como \"$nombreFinal\"",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Genera un nombre único para el nuevo pedido con formato "PyyyyMMdd" o "PyyyyMMdd(n)".
     */
    private suspend fun nombrePedidoUnico(): String {
        val fechaHoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        var baseNombre = "P$fechaHoy"
        var nombreCandidato = baseNombre
        var contador = 1

        // Leer los pedidos ya existentes en BD para comprobar duplicados
        val pedidosExistentes = withContext(Dispatchers.IO) {
            pedidoDao.obtenerPedidosConGraficos(userId).map { it.pedido.nombre }
        }
        while (pedidosExistentes.any { it == nombreCandidato }) {
            nombreCandidato = "$baseNombre($contador)"
            contador++
        }
        return nombreCandidato
    }


    /**
     * Cuando vuelves desde GraficoPedido (REQUEST_CODE_GRAFICO_PEDIDO),
     * actualizamos la entidad HiloGrafico en DB y la lista en memoria.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GRAFICO_PEDIDO && resultCode == RESULT_OK && data != null) {
            val graficoEditado = data.getSerializableExtra("grafico") as? Grafico
            val posicion = data.getIntExtra("position", -1)

            if (graficoEditado != null && posicion in listaGraficos.indices) {
                lifecycleScope.launch(Dispatchers.IO) {

                    val graficoEnt =
                        graficoDao.obtenerGraficoEnCursoPorNombre(userId, graficoEditado.nombre)
                    if (graficoEnt != null) {
                        val graficoId = graficoEnt.id
                        val hilosPrevios = hiloGraficoDao.obtenerHilosDeGrafico(graficoId)
                        for (prev in hilosPrevios) {
                            hiloGraficoDao.eliminarHiloDeGrafico(prev)
                        }
                        for (hiloDom in graficoEditado.listaHilos) {
                            hiloGraficoDao.insertarHiloEnGrafico(
                                HiloGraficoEntity(
                                    graficoId = graficoId,
                                    hilo = hiloDom.hilo,
                                    madejas = hiloDom.madejas
                                )
                            )
                        }
                    }
                    // 2) Actualizar la lista en memoria y la UI
                    withContext(Dispatchers.Main) {
                        listaGraficos[posicion] = graficoEditado
                        adaptadorPedido.notifyItemChanged(posicion)
                        actualizarTotalMadejas()
                    }
                }
            }
        }
    }

}