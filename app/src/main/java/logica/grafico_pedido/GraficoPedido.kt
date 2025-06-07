package logica.grafico_pedido

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.pedido_hilos.Grafico
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloGraficoDao
import persistencia.daos.HiloStockDao
import persistencia.entidades.GraficoEntity
import persistencia.entidades.HiloGraficoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog
import utiles.funciones.calcularMadejas
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

/**
 * Actividad para visualizar y editar un gráfico individual dentro de un pedido.
 *
 * Muestra:
 *  - Lista de hilos y sus madejas necesarias.
 *  - Stock actual de cualquier hilo al pulsarlo.
 *  - Total de madejas del gráfico.
 *
 * Permite:
 *  - Añadir nuevos hilos al gráfico (solicitando puntadas y opcionalmente el count de tela).
 *  - Eliminar hilos existentes con confirmación.
 *  - Buscar hilos en la lista.
 *  - Persistir en Room el gráfico y sus hilos (incluyendo count de tela).
 *  - Al volver, devuelve a la actividad padre un objeto [Grafico] actualizado.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class GraficoPedido : BaseActivity() {

    /** Adaptador para mostrar y editar la lista de hilos en el gráfico */
    private lateinit var adaptadorGrafico: AdaptadorGrafico

    /** DAO para operaciones CRUD sobre HiloGraficoEntity */
    private lateinit var daoGrafico: HiloGraficoDao

    /** DAO para consultar stock de hilos en Room */
    private lateinit var daoStock: HiloStockDao

    /** TextView que muestra el total de madejas del gráfico */
    private lateinit var txtTotal: TextView

    /** TextView que muestra el stock actual del hilo seleccionado */
    private lateinit var txtStockActual: TextView

    /** RecyclerView para listar los hilos del gráfico */
    private lateinit var recyclerView: RecyclerView

    /** Lista en memoria de objetos de dominio [HiloGrafico] */
    private var listaDominio: MutableList<HiloGrafico> = mutableListOf()

    /** Nombre identificativo del gráfico (recibido desde la actividad padre 'PedidoHilos') */
    private var graficoNombre: String = ""

    /** ID interno en Room de este gráfico (tabla GraficoEntity) */
    private var graficoId: Int = -1

    /**
     * Count de tela (hilos por pulgada) persistido en GraficoEntity.count.
     * Se solicita sólo la primera vez y se oculta tras persistirlo.
     */
    private var countTelaGlobal: Int? = null

    /** Identificador del usuario actual obtenido de [SesionUsuario] */
    private var userId: Int = -1

    /**
     * Inicializa la UI, obtiene o crea la entidad [GraficoEntity], carga el count,
     * y llama a configurar el RecyclerView y los botones.
     *
     * @param savedInstanceState Estado previo de la actividad.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidob_aa_principal)
        funcionToolbar(this)

        /* instanciar DAOs y obtener sesión */
        daoGrafico = ThreadlyDatabase.getDatabase(applicationContext).hiloGraficoDao()
        daoStock = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        /* inicializar vistas */
        txtTotal = findViewById(R.id.txtVw_totalMadejasGraficoIndividual)
        txtStockActual = findViewById(R.id.txtVw_stockHiloActual)
        recyclerView = findViewById(R.id.tabla_grafico)
        recyclerView.layoutManager = LinearLayoutManager(this)

        /* recibir el objeto Grafico desde PedidoHilos */
        val graficoRecibido = intent.getSerializableExtra("grafico") as? Grafico
        if (graficoRecibido == null) {
            Toast.makeText(this, "Error: gráfico no recibido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        graficoNombre = graficoRecibido.nombre
        findViewById<TextView>(R.id.txtVw_cabeceraGrafico).text = graficoNombre

        /* carga o inserta la entidad en Room y recupera el countTela */
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                var existingId = daoGrafico.obtenerIdPorNombre(graficoNombre)
                if (existingId == null) {
                    existingId = daoGrafico.insertarGrafico(
                        GraficoEntity(
                            nombre = graficoNombre,
                            idPedido = null,
                            userId = userId,
                            count = null
                        )
                    ).toInt()
                }
                graficoId = existingId
                countTelaGlobal = daoGrafico.obtenerCountTela(graficoId)
            }

            /* si falla al obtener un ID válido, finaliza */
            if (graficoId < 0) {
                Toast.makeText(this@GraficoPedido, "Error cargando gráfico", Toast.LENGTH_SHORT)
                    .show()
                finish()
                return@launch
            }

            configurarRecycler()
            configurarBotones()
        }
    }

    /**
     * Configura el RecyclerView con [AdaptadorGrafico]:
     * - Carga hilos desde Room, los mapea a dominio y ordena.
     * - Asigna callbacks para clic, clic largo y cambios de madejas.
     * - Muestra el total inicial de madejas.
     */
    @SuppressLint("SetTextI18n")
    private fun configurarRecycler() {
        lifecycleScope.launch {
            val entidades = withContext(Dispatchers.IO) {
                daoGrafico.obtenerHilosPorGrafico(graficoId)
            }

            /* mapear entidades a dominio y ordenar */
            listaDominio = entidades.map { HiloGrafico(it.hilo, it.madejas) }
                .let { ordenarHilos(it) { h -> h.hilo } }
                .toMutableList()

            if (!::adaptadorGrafico.isInitialized) {  /* verifica si  'adaptadorGrafico' ha sido inicializada antes de usarla, evita crashear */
                adaptadorGrafico = AdaptadorGrafico(
                    listaDominio,
                    onClickHilo = { hiloGrafico ->
                        val hiloActual = hiloGrafico.hilo
                        val yaResaltado = adaptadorGrafico.obtenerHiloResaltadoClick() == hiloActual
                        adaptadorGrafico.resaltarHiloClick(hiloActual)
                        if (yaResaltado) {
                            txtStockActual.text = "Stock: 0"
                        } else {
                            mostrarStock(hiloActual)
                        }
                    },
                    onBorrarHilo = ::dialogBorrarHilo,
                    onTotalChanged = { total ->
                        txtTotal.text = "Total Madejas: $total"
                    },
                    onUpdateMadejas = { hiloGrafico ->
                        /* persistir la nueva cantidad de madejas en Room */
                        lifecycleScope.launch(Dispatchers.IO) {
                            daoGrafico.actualizarMadejas(hiloGrafico.hilo, hiloGrafico.madejas)
                        }
                    }
                )
                recyclerView.adapter = adaptadorGrafico
            } else {
                adaptadorGrafico.actualizarLista(listaDominio)
            }

            /* mostrar total inicial */
            val total = listaDominio.sumOf { it.madejas }
            txtTotal.text = "Total Madejas: $total"
            buscadorGrafico()
        }
    }

    /**
     * Configura botones de añadir hilo y volver:
     * - [R.id.btn_agregarHiloGraficoIndividual]: abre diálogo de inserción.
     * - [R.id.btn_volver_pedido_desde_grafico]: devuelve resultado y cierra.
     */
    private fun configurarBotones() {
        findViewById<Button>(R.id.btn_agregarHiloGraficoIndividual).setOnClickListener {
            dialogAgregarHiloGrafico()
        }
        findViewById<Button>(R.id.btn_volver_pedido_desde_grafico).setOnClickListener {
            devolverResultadoYSalir()
        }
    }

    /**
     * Habilita un buscador para filtrar hilos por código:
     * - Oculta el teclado al buscar.
     * - Resalta o muestra “sin resultados” si no encuentra.
     * - Restaura lista y quita resaltado si el texto se borra.
     */
    private fun buscadorGrafico() {
        val hiloBuscar = findViewById<EditText>(R.id.edTxt_buscadorGrafico)
        val btnLupa = findViewById<ImageView>(R.id.imgVw_lupaGrafico)
        val sinResultados = findViewById<TextView>(R.id.txtVw_sinResultadosGrafico)
        sinResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(hiloBuscar.windowToken, 0)

            val codigoHilo = hiloBuscar.text.toString().trim().uppercase()
            val encontrado = listaDominio.find { it.hilo == codigoHilo }
            if (encontrado != null) {
                adaptadorGrafico.resaltarHiloBusqueda(encontrado.hilo)
                adaptadorGrafico.actualizarLista(listaDominio)
                recyclerView.scrollToPosition(listaDominio.indexOf(encontrado))
                sinResultados.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                sinResultados.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }

        hiloBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorGrafico.resaltarHiloBusqueda(null)
                    adaptadorGrafico.actualizarLista(listaDominio)
                    sinResultados.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Consulta Room el stock actual de madejas para un hilo dado
     * y lo muestra en [txtStockActual].
     *
     * @param hilo Código del hilo a consultar.
     */
    private fun mostrarStock(hilo: String) {
        lifecycleScope.launch {
            val stock = withContext(Dispatchers.IO) {
                daoStock.obtenerMadejas(userId, hilo)
            }
            txtStockActual.text = "Stock: ${stock ?: 0}"
        }
    }

    /**
     * Diálogo para añadir un hilo al gráfico:
     * - Solicita código, puntadas y opcionalmente count de tela.
     * - Valida campos, existencia en catálogo y duplicados.
     * - Calcula madejas con [calcularMadejas], persiste count y el hilo en Room.
     * - Actualiza lista, total y adaptador.
     */
    @SuppressLint("SetTextI18n")
    private fun dialogAgregarHiloGrafico() {
        lifecycleScope.launch {
            /* sólo pedir count la primera vez */
            val necesitaCount = (countTelaGlobal == null)

            withContext(Dispatchers.Main) {
                val dialog = Dialog(this@GraficoPedido).apply {
                    setContentView(R.layout.pedidob_dialog_agregar_hilo)
                    window?.setBackgroundDrawableResource(android.R.color.transparent)
                    ajustarDialog(this)
                    setCancelable(false)
                }

                val numeroHilo =
                    dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
                val pedirPuntadas =
                    dialog.findViewById<EditText>(R.id.edTxt_introducirPuntadas_dialog_addHilo)
                val pedirCount = dialog.findViewById<EditText>(R.id.edTxt_pedirCountTela)
                val btnGuardar =
                    dialog.findViewById<Button>(R.id.btn_guardar_dialog_pedidob_addHilo)

                dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_addHilo)
                    .setOnClickListener { dialog.dismiss() }

                /* ocultar el campo count si ya existe en Room */
                if (!necesitaCount) {
                    pedirCount.visibility = View.GONE
                } else {
                    pedirCount.visibility = View.VISIBLE
                }

                btnGuardar.setOnClickListener {
                    val hiloCode = numeroHilo.text.toString().trim().uppercase()
                    val punt = pedirPuntadas.text.toString().trim().toIntOrNull()

                    if (hiloCode.isEmpty() || punt == null) {
                        Toast.makeText(this@GraficoPedido, "Campos inválidos", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }
                    /* evitar duplicados en memoria */
                    if (listaDominio.any { it.hilo == hiloCode }) {
                        Toast.makeText(
                            this@GraficoPedido,
                            "El hilo ya se ha añadido al gráfico",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    lifecycleScope.launch {
                        /* comprobar existencia en catálogo */
                        val existeEnCatalogo = withContext(Dispatchers.IO) {
                            ThreadlyDatabase.getDatabase(applicationContext)
                                .hiloCatalogoDao()
                                .obtenerHiloPorNumYUsuario(hiloCode, userId) != null
                        }
                        if (!existeEnCatalogo) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@GraficoPedido,
                                    "El hilo no está en tu catálogo. Añádelo primero.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@launch
                        }

                        /* si hace falta count (primera inserción), capturarlo y persistirlo */
                        if (necesitaCount) {
                            val countTela = pedirCount.text.toString().trim().toIntOrNull()
                            if (countTela == null || countTela <= 0) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@GraficoPedido,
                                        "Count de tela inválido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@launch
                            }
                            countTelaGlobal = countTela /* guardar en memoria */
                            /* persistir en Room */
                            withContext(Dispatchers.IO) {
                                daoGrafico.actualizarCountTela(graficoId, countTela)
                            }
                        }

                        /* adquirido el count, se calculan las meadejas con la función de útiles */
                        val madejas = calcularMadejas(punt, countTelaGlobal!!)

                        /* insertar en Room */
                        withContext(Dispatchers.IO) {
                            daoGrafico.insertarHiloEnGrafico(
                                HiloGraficoEntity(
                                    graficoId = graficoId,
                                    hilo = hiloCode,
                                    madejas = madejas
                                )
                            )
                        }

                        /* actualizar lista en memoria y adaptador */
                        listaDominio.add(HiloGrafico(hiloCode, madejas))
                        listaDominio = ordenarHilos(listaDominio) { it.hilo }.toMutableList()

                        withContext(Dispatchers.Main) {
                            adaptadorGrafico.actualizarLista(listaDominio)
                            txtTotal.text = "Total Madejas: ${listaDominio.sumOf { it.madejas }}"
                            dialog.dismiss()
                        }
                    }
                }

                dialog.show()
            }
        }
    }

    /**
     * Diálogo para eliminar un hilo del gráfico:
     * - Muestra confirmación con el código pintado en rojo.
     * - Al confirmar, borra la entidad en Room, recarga la lista y, si queda vacía,
     *   resetea el countTela en memoria y en la base de datos.
     *
     * @param h Instancia de [HiloGrafico] a eliminar.
     */
    private fun dialogBorrarHilo(h: HiloGrafico) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.pedidob_dialog_borrar_hilo)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            ajustarDialog(this)
        }

        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_guardarHilo_dialog_deleteHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_dialog_pedidob_deleteHilo)
        val txtMsg = dialog.findViewById<TextView>(R.id.txtVw_textoInfo_dialog_deleteHilo)

        /* pintar el nombre en rojo */
        val texto = getString(R.string.textoInfo_dialog_deleteHilo).replace("%s", h.hilo)
        txtMsg.text = SpannableString(texto).apply {
            val inicio = texto.indexOf(h.hilo)
            val fin = inicio + h.hilo.length
            setSpan(ForegroundColorSpan(Color.RED), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            lifecycleScope.launch {
                /* borrar de Room */
                withContext(Dispatchers.IO) {
                    daoGrafico.eliminarHiloDeGrafico(graficoId, h.hilo)
                }
                /* recargar lista */
                withContext(Dispatchers.Main) {
                    configurarRecycler()
                    /* si ya no quedan hilos, resetear countTelaGlobal */
                    if (listaDominio.isEmpty()) {
                        countTelaGlobal = null
                        lifecycleScope.launch(Dispatchers.IO) {
                            daoGrafico.actualizarCountTela(graficoId, null)
                        }
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    /**
     * Empaqueta el gráfico actualizado en un [Intent], lo devuelve al padre y cierra.
     * Incluye nombre, lista de hilos y posición original.
     */
    private fun devolverResultadoYSalir() {
        val listaFinal = adaptadorGrafico.obtenerLista()
        val resultado = Grafico(
            nombre = graficoNombre,
            listaHilos = listaFinal
        )
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra("grafico", resultado)
                putExtra("position", intent.getIntExtra("position", -1))
            }
        )
        finish()
    }
}
