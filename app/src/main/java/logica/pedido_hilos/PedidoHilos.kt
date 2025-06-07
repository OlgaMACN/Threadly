package logica.pedido_hilos

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
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
 * Código de solicitud para editar un gráfico dentro de un pedido.
 * Se usa en `startActivityForResult` y `onActivityResult`.
 */
private val REQUEST_CODE_GRAFICO_PEDIDO = 1 /* para identificar cada gráfico */

/**
 * Actividad principal para crear y gestionar pedidos de hilos.
 * Permite:
 *  - Añadir, editar y eliminar "gráficos" (conjuntos de hilos y madejas).
 *  - Visualizar el total de madejas de todos los gráficos.
 *  - Guardar el pedido en la base de datos.
 *  - Realizar el pedido en tiendas externas (Amazon, AliExpress, Temu).
 *
 * Se conecta a Room mediante DAOs [GraficoDao], [HiloGraficoDao] y [PedidoDao],
 * y gestiona una lista en memoria de objetos [Grafico].
 *
 * Hereda de [BaseActivity] para reutilizar la configuración de toolbar.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class PedidoHilos : BaseActivity() {

    /** Adaptador para renderizar la lista de gráficos en un RecyclerView */
    private lateinit var adaptadorPedido: AdaptadorPedido

    /** Lista mutable en memoria de gráficos incluidos en el pedido actual */
    private var listaGraficos: MutableList<Grafico> = mutableListOf()

    /** Botón que confirma y guarda el pedido en la base de datos */
    private lateinit var btnGuardarPedido: Button

    /** Identificador del usuario actualmente logueado */
    private var userId: Int = -1

    /** DAOs para acceso a Room */
    private lateinit var graficoDao: GraficoDao
    private lateinit var hiloGraficoDao: HiloGraficoDao
    private lateinit var pedidoDao: PedidoDao

    /**
     * Punto de entrada de la actividad.
     * - Infla el layout `pedido_aa_principal`.
     * - Configura toolbar con [funcionToolbar].
     * - Inicializa DAOs y obtiene `userId` de [SesionUsuario].
     * - Configura RecyclerView y su adaptador [AdaptadorPedido].
     * - Configura listeners de botones y buscador.
     * - Carga en memoria los gráficos en curso y actualiza total de madejas.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* instancias de los DAOs */
        graficoDao = ThreadlyDatabase.getDatabase(applicationContext).graficoDao()
        hiloGraficoDao = ThreadlyDatabase.getDatabase(applicationContext).hiloGraficoDao()
        pedidoDao = ThreadlyDatabase.getDatabase(applicationContext).pedidoDao()

        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        /* inicialización del RecyclerView y Adaptador */
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)
        adaptadorPedido = AdaptadorPedido(
            listaGraficos,
            onEditarGrafico = { graficoSeleccionado ->
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
        buscadorPedido()
        actualizarTotalMadejas()
    }

    /**
     * Carga en memoria todos los [GraficoEntity] no asociados a ningún pedido
     * (idPedido = NULL) para el `userId` actual, junto con sus [HiloGraficoEntity].
     * Transforma el dominio [Grafico] y actualiza el adaptador.
     */
    private fun cargarGraficosTemporalesEnMemoria() {
        lifecycleScope.launch {
            val entidadesGrafico = withContext(Dispatchers.IO) {
                graficoDao.obtenerGraficosEnCurso(userId)
            }

            /* para cada GraficoEntity, se necesita leer sus HiloGraficoEntity y mapearlos a HiloGrafico */
            listaGraficos.clear()
            for (graficoEntity in entidadesGrafico) {
                val hilosEntidades = withContext(Dispatchers.IO) {
                    hiloGraficoDao.obtenerHilosDeGrafico(graficoEntity.id)
                }
                /* mapear a dominio: Grafico(nombre, listaHilos=MutableList<HiloGrafico>) */
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

            /* orden alfabético por nombre de gráfico */
            listaGraficos.sortBy { it.nombre.lowercase() }
            adaptadorPedido.actualizarLista(listaGraficos)
            actualizarTotalMadejas()
            validarBotonGuardar()
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de guardar el pedido.
     * Si la lista de gráficos está vacía, informa al usuario.
     * Al confirmar, llama a [guardarPedidoEnBD].
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

    /**
     * Habilita o deshabilita el botón de guardar según `listaGraficos` esté vacía o no.
     * Ajusta la opacidad de [btnGuardarPedido].
     */
    private fun validarBotonGuardar() {
        val habilitado = listaGraficos.isNotEmpty()
        btnGuardarPedido.alpha = if (habilitado) 1.0f else 0.5f
    }

    /**
     * Configura un buscador para filtrar y resaltar gráficos por nombre:
     * - Oculta el teclado al buscar.
     * - Restaura la lista si el campo está vacío.
     * - Muestra "Sin resultados" si no encuentra coincidencias.
     */
    private fun buscadorPedido() {
        val buscarPedido = findViewById<EditText>(R.id.edTxt_buscadorPedido)
        val btnLupaPedido = findViewById<ImageButton>(R.id.imgBtn_lupaPedido)
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosPedido)
        txtNoResultados.visibility = View.GONE

        btnLupaPedido.setOnClickListener {
            /* ocultar el teclado */
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(buscarPedido.windowToken, 0)

            val busqueda = buscarPedido.text.toString().trim().uppercase()
            if (busqueda.isEmpty()) {
                /* si el usuario no escribió nada, se restaura la tabla y sale */
                adaptadorPedido.resaltarGrafico(null)
                adaptadorPedido.actualizarLista(listaGraficos)
                actualizarTotalMadejas()
                tablaPedido.visibility = View.VISIBLE
                txtNoResultados.visibility = View.GONE
                return@setOnClickListener
            }

            /* fltrar todos los gráficos cuyo nombre contenga la subcadena 'busqueda' (no case sensitive) */
            val coincidencias = listaGraficos.filter {
                it.nombre.uppercase().contains(busqueda)
            }

            if (coincidencias.isNotEmpty()) {
                /* ordenar las coincidencias alfabéticamente por nombre (lowercase para ser consistente) */
                val primera = coincidencias
                    .sortedBy { it.nombre.lowercase() }
                    .first()

                /* calcular el índice original en listaGraficos, según el nombre */
                val indexOriginal = listaGraficos.indexOfFirst {
                    it.nombre == primera.nombre
                }

                /* resaltar y desplazar al gráfico encontrado */
                adaptadorPedido.resaltarGrafico(primera.nombre)
                adaptadorPedido.actualizarLista(listaGraficos) /* para que aplique el resaltado */
                tablaPedido.visibility = View.VISIBLE
                txtNoResultados.visibility = View.GONE

                /* desplazamiento (post para asegurar que RecyclerView ya está listo) */
                tablaPedido.post {
                    tablaPedido.scrollToPosition(indexOriginal)
                }
            } else {
                /* si no hay coincidencias, se oculta la tabla y muestra el mensaje configurado */
                tablaPedido.visibility = View.GONE
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        /* si el texto de búsqueda está vacío, se restaura la tabla completa sin resaltados */
        buscarPedido.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorPedido.resaltarGrafico(null)
                    adaptadorPedido.actualizarLista(listaGraficos)
                    actualizarTotalMadejas()
                    tablaPedido.visibility = View.VISIBLE
                    txtNoResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Calcula y muestra el total de madejas de todos los gráficos
     * en el TextView `txtVw_madejasTotalPedido`.
     */
    private fun actualizarTotalMadejas() {
        val txtTotal = findViewById<TextView>(R.id.txtVw_madejasTotalPedido)
        val total = listaGraficos.sumOf { grafico ->
            grafico.listaHilos.sumOf { hiloGrafico -> hiloGrafico.madejas }
        }
        txtTotal.text = "Total madejas: $total"
    }

    /**
     * Muestra un diálogo para añadir un nuevo gráfico al pedido:
     * 1. Valida nombre no vacío.
     * 2. Evita duplicados en la lista en memoria.
     * 3. Consulta en BD para advertir de nombres ya usados.
     * 4. Inserta el gráfico en curso si no existe.
     * 5. Actualiza lista, ordena, refresca UI y cierra el diálogo.
     */
    private fun dialogAgregarGrafico() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_agregar_grafico)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val nombreGrafico = dialog.findViewById<EditText>(R.id.edTxt_pedirNombreGrafico)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarGrafico)
        val btnCancelar = dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog_agregarGrafico)

        btnGuardar.setOnClickListener {
            val nombre = nombreGrafico.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /* evitar duplicados en la lista en memoria (pedido en curso) */
            if (listaGraficos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                Toast.makeText(
                    this,
                    "Ya existe un gráfico con ese nombre en el pedido actual",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            /* consultar en la BdD si ya existe el gráfico nuevo (sea en curso o ya guardado) */
            lifecycleScope.launch(Dispatchers.IO) {
                val enCualquierPedido = graficoDao.obtenerGraficoPorNombre(nombre)

                withContext(Dispatchers.Main) {
                    if (enCualquierPedido != null) {
                        /** Si se encuentra en la BdD un Gráfico con ese nombre (ya sea idPedido=null o ≠null),
                        se muestra un Toast informativo, pero NO se impide seguir, porque solo es útil bloquear
                        la creación duplicada en el mismo pedido en curso */
                        Toast.makeText(
                            this@PedidoHilos,
                            "Ese gráfico existe en un pedido anterior, tú sabrás...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                /* consultar si existe el gráfico en el pedido 'en curso' (idPedido=null) para el usuario actual */
                val yaEnCurso = graficoDao.obtenerGraficoEnCursoPorNombre(userId, nombre)
                if (yaEnCurso == null) {
                    /* si no es el caso, se puede insertar sin problema */
                    graficoDao.insertarGrafico(
                        persistencia.entidades.GraficoEntity(
                            nombre = nombre,
                            idPedido = null,
                            userId = userId
                        )
                    )
                }
                /* si ya estaba en curso, se salta la inserción (evitando el UNIQUE contra (userId, nombre, idPedido=null)) */

                /* finalmente, se añade el dominio en memoria (listaGraficos) y se refresca la UI */
                withContext(Dispatchers.Main) {
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
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    /**
     * Muestra un diálogo de confirmación para eliminar un gráfico del pedido:
     * - Borra entidades en Room (HiloGrafico y GraficoEntity).
     * - Actualiza lista en memoria y UI.
     *
     * @param index Índice del gráfico a eliminar en [listaGraficos].
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
                /* primero se obtiene el id de GraficoEntity por nombre (solo temporales) */
                val entidad = graficoDao.obtenerGraficoEnCursoPorNombre(userId, graficoDom.nombre)
                if (entidad != null) {
                    /* se borran todos sus HiloGraficoEntity relacionados */
                    val hilosEnt = hiloGraficoDao.obtenerHilosDeGrafico(entidad.id)
                    for (h in hilosEnt) {
                        hiloGraficoDao.eliminarHiloDeGrafico(h)
                    }
                    /* y se borra el propio grafico */
                    graficoDao.eliminarGraficoEnCurso(entidad.id)
                }
                /* actualizar la UI en el hilo principal */
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
     * Muestra un diálogo con iconos de tiendas externas para realizar el pedido.
     * Al pulsar, intenta abrir la app; si no está instalada, abre la web.
     * Las tiendas pensadas para este caso han sido Amazon, AliExpress y Temu.
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
            Toast.makeText(this, "Error al redirigir a la tienda", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Guarda el pedido completo en Room:
     * - Genera un nombre único con fecha (ddMMyy) y sufijo si es necesario.
     * - Inserta `PedidoEntity` en la tabla `pedidos`.
     * - Asocia todos los gráficos en curso a ese pedido.
     * - Limpia la lista en memoria y actualiza la interfaz.
     */
    private fun guardarPedidoEnBD() {
        lifecycleScope.launch {
            /* antes de acceder a Room, se definen algunas variables que se usarán más adelante */
            val fechaHoy = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())
            val baseNombre = "P$fechaHoy"  /* por ejemplo "P020625" */
            var contador = 0
            var nombreFinal: String
            var nuevoPedidoId: Int? = null

            /* lectura/inserción en IO */
            withContext(Dispatchers.IO) {
                while (nuevoPedidoId == null) {
                    /* para generar el candidato de nombre, si el contador = 0, se usa "P060625". si >0, se usa "P060625_1", etc. */
                    nombreFinal = if (contador == 0) {
                        baseNombre
                    } else {
                        "${baseNombre}_$contador"
                    }

                    try {
                        /** Se intenta insertar el nuevo PedioEntity.
                        Si falla por UNIQUE sobre “nombre”, saltará SQLiteConstraintException */
                        nuevoPedidoId = pedidoDao.insertarPedido(
                            PedidoEntity(
                                nombre = nombreFinal,
                                userId = userId
                            )
                        ).toInt()

                        /* si la inserción es correcta, se pueden asociar los gráficos al pedido */
                        graficoDao.asociarGraficosAlPedido(userId, nuevoPedidoId!!)
                        /* y se puede salir del bucle porque ya tenemos el ID ;) */
                    } catch (e: android.database.sqlite.SQLiteConstraintException) {
                        /** Si entra en el catch, es que "nombreFinal" ya existe en la tabla `pedidos`.
                        Para solventarlo, se incrementa el sufijo numérico y vuelve a probar */
                        contador++
                        /* nada más, solo dejar que termine este catch y se repita el while hasta encontrar un sufijo válido */
                    }
                }
            }

            /* inserción completada y nuevoPedidoId != null */
            listaGraficos.clear()   /* limpiar la UI en el hilo principal */
            adaptadorPedido.actualizarLista(listaGraficos)
            actualizarTotalMadejas()
            validarBotonGuardar()
            Toast.makeText(
                this@PedidoHilos,
                "Pedido guardado como \"${
                    /* se reconstruye el nombre que realmente se usó */
                    if (contador == 0) baseNombre else "${baseNombre}_$contador"
                }\"",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Recepción del resultado desde [GraficoPedido].
     * Actualiza la base de datos y la lista en memoria con los hilos modificados.
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
                    withContext(Dispatchers.Main) {
                        listaGraficos[posicion] = graficoEditado
                        adaptadorPedido.notifyItemChanged(posicion)
                        actualizarTotalMadejas()
                        validarBotonGuardar()
                    }
                }
            }
        }
    }


}