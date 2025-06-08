package logica.stock_personal

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloStockDao
import persistencia.entidades.HiloStockEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ValidarFormatoHilos
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

/**
 * Actividad que gestiona el inventario personal de hilos del usuario.
 * Permite visualizar el stock de madejas, añadir nuevos hilos,
 * incrementar o decrementar madejas, eliminar hilos completos y buscar
 * un hilo por código. Utiliza un RecyclerView con [AdaptadorStock]
 * para mostrar los elementos.
 *
 * Esta clase hereda de [BaseActivity] para reutilizar comportamiento común
 * como la configuración de la toolbar.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class StockPersonal : BaseActivity() {

    /**
     * RecyclerView que muestra la lista de hilos y sus madejas.
     */
    private lateinit var tablaStock: RecyclerView

    /**
     * Adaptador que gestiona la lista interna de [HiloStock] y renderiza
     * cada fila, además de manejar el evento de eliminación.
     */
    private lateinit var adaptadorStock: AdaptadorStock

    /**
     * DAO para acceso a las operaciones CRUD de [HiloStockEntity] en Room.
     */
    private lateinit var dao: HiloStockDao

    /**
     * Identificador del usuario actualmente conectado.
     * Se obtiene mediante [SesionUsuario.obtenerSesion].
     */
    private var userId: Int = -1

    /**
     * Lista mutable local que contiene los objetos [HiloStock] leídos
     * de la base de datos y se pasa al adaptador.
     */
    private val listaStock = mutableListOf<HiloStock>()

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)
        funcionToolbar(this)

        /* inicializar DAO y sesión de usuario */
        dao = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()  /* si no hay sesión activa, cierra la actividad */

        /* configuración del RecyclerView y su adaptador */
        tablaStock = findViewById(R.id.tabla_stock)
        adaptadorStock = AdaptadorStock(listaStock, ::dialogEliminarHilo)
        tablaStock.layoutManager = LinearLayoutManager(this)
        tablaStock.adapter = adaptadorStock

        /* switch para alternar el orden de la tabla (por cantidad o alfabético) */
        val switchOrdenar = findViewById<Switch>(R.id.switch_orden_stock)
        switchOrdenar.setOnCheckedChangeListener { _, isChecked ->
            ordenarListaYActualizarUI(isChecked)
            /* deshabilita brevemente el switch para evitar clics rápidos consecutivos */
            switchOrdenar.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                switchOrdenar.isEnabled = true
            }, 500)
        }

        /* primera carga de datos en segundo plano */
        lifecycleScope.launch(Dispatchers.IO) {
            refrescarUI()
        }

        /* botones para los diálogos de agregar hilo, sumar madejas y restar madejas */
        findViewById<Button>(R.id.btn_agregarHiloStk).setOnClickListener { dialogAgregarHilo() }
        findViewById<Button>(R.id.btn_agregarMadejaStk).setOnClickListener { dialogAgregarMadeja() }
        findViewById<Button>(R.id.btn_eliminarMadejaStk).setOnClickListener { dialogEliminarMadeja() }

        /* buscador de hilos en el stock */
        buscadorStock()
    }

    /**
     * Ordena [listaStock] según [ordenPorCantidad]:
     * - Si true: ordena por madejas descendente y luego por código.
     * - Si false: ordena alfabéticamente según el ID de hilo.
     * Notifica al adaptador del cambio en el switch.
     *
     * @param ordenPorCantidad indica si ordenar por cantidad de madejas.
     */
    private fun ordenarListaYActualizarUI(ordenPorCantidad: Boolean) {
        val ordenada = if (ordenPorCantidad) {
            listaStock
                .sortedWith(compareByDescending<HiloStock> { it.madejas }
                    .thenBy { it.hiloId })
        } else {
            ordenarHilos(listaStock) { it.hiloId }
        }

        listaStock.apply {
            clear()
            addAll(ordenada)
        }
        adaptadorStock.actualizarLista(listaStock)
    }

    /**
     * Lee el stock del usuario desde la base de datos y actualiza
     * [listaStock], aplicando el orden actual del switch.
     * Ejecuta la lectura en [Dispatchers.IO] y la actualización de UI
     * en el hilo principal.
     */
    private fun refrescarUI() {
        lifecycleScope.launch(Dispatchers.IO) {
            val entidades = dao.obtenerStockPorUsuario(userId)
            val dominio = entidades.map { HiloStock(it.hiloId, it.madejas) }

            withContext(Dispatchers.Main) {
                listaStock.apply {
                    clear()
                    addAll(dominio)
                }
                val ordenarPorCantidad = findViewById<Switch>(R.id.switch_orden_stock).isChecked
                ordenarListaYActualizarUI(ordenarPorCantidad)
            }
        }
    }

    /**
     * Configura el buscador de hilos por código:
     * - Oculta el teclado al buscar.
     * - Si encuentra el hilo, lo resalta y desplaza la tabla a su posición.
     * - Si no, oculta la tabla y muestra un mensaje “sin resultados”.
     * - Al borrar el texto, restaura la lista completa y quita el resaltado.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun buscadorStock() {
        val hiloBuscar = findViewById<EditText>(R.id.edTxt_buscadorHilo)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaStock)
        val sinResultados = findViewById<TextView>(R.id.txtVw_sinResultados)
        sinResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            /* oculta el teclado */
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(hiloBuscar.windowToken, 0)

            val busqueda = hiloBuscar.text.toString().trim().uppercase()
            val encontrado = listaStock.find { it.hiloId == busqueda }

            if (encontrado != null) {
                val idx = listaStock.indexOf(encontrado)
                adaptadorStock.resaltarHilo(encontrado.hiloId)
                adaptadorStock.notifyDataSetChanged()
                tablaStock.scrollToPosition(idx)
                tablaStock.visibility = View.VISIBLE
                sinResultados.visibility = View.GONE
            } else {
                tablaStock.visibility = View.GONE
                sinResultados.visibility = View.VISIBLE
            }
        }

        hiloBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorStock.resaltarHilo(null)
                    adaptadorStock.actualizarLista(listaStock)
                    tablaStock.visibility = View.VISIBLE
                    sinResultados.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Muestra un diálogo para añadir un nuevo hilo al stock:
     * - Valida que los campos no estén vacíos y el formato del ID.
     * - Verifica que el hilo exista en el catálogo.
     * - Verifica que aún no esté en el stock del usuario.
     * - Inserta la entidad [HiloStockEntity] en la base de datos.
     * - Refresca la UI y cierra el diálogo.
     */
    private fun dialogAgregarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val numeroHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val madejas = dialog.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)
        dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_agregarHilo)
            .setOnClickListener { dialog.dismiss() }

        dialog.findViewById<Button>(R.id.btn_botonAgregarHiloStk).setOnClickListener {
            val hilo = numeroHilo.text.toString().trim().uppercase()
            val madejas = madejas.text.toString().toIntOrNull() ?: -1

            if (hilo.isEmpty() || madejas < 0) {
                Toast.makeText(this, "Campos inválidos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!ValidarFormatoHilos.formatoValidoHilo(hilo)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val BdD = ThreadlyDatabase.getDatabase(applicationContext)
                val daoStock = BdD.hiloStockDao()
                val daoCatalogo = BdD.hiloCatalogoDao()

                /* verificar si el hilo está en el catálogo */
                val existeEnCatalogo = daoCatalogo.obtenerHiloPorNumYUsuario(hilo, userId) != null
                if (!existeEnCatalogo) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@StockPersonal,
                            "El hilo no está en tu catálogo. Añádelo primero...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                /* verificar que no esté ya en el stock */
                val yaExisteStock = daoStock.obtenerPorHiloUsuario(hilo, userId) != null
                if (yaExisteStock) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@StockPersonal,
                            "Ya tienes ese hilo en el stock",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                /* insertar el nuevo hilo en el stock */
                daoStock.insertarStock(
                    HiloStockEntity(usuarioId = userId, hiloId = hilo, madejas = madejas)
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StockPersonal, "Hilo añadido correctamente", Toast.LENGTH_SHORT).show()
                    refrescarUI()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    /**
     * Muestra un diálogo para sumar madejas a un hilo existente:
     * - Al escribir el código, muestra las madejas actuales.
     * - Habilita el campo y botón si el hilo existe.
     * - Al confirmar, actualiza la entidad con el nuevo total de madejas.
     */
    private fun dialogAgregarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val hilo = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk_hilo)
        val madejas = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk)
        val madejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarMadejaStk)
        dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)
            .setOnClickListener { dialog.dismiss() }

        hilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val code = s.toString().trim().uppercase()
                lifecycleScope.launch(Dispatchers.IO) {
                    val ent = dao.obtenerPorHiloUsuario(code, userId)
                    withContext(Dispatchers.Main) {
                        if (ent != null) {
                            madejasActuales.text = "Madejas actuales: ${ent.madejas}"
                            madejas.isEnabled = true
                            btnGuardar.isEnabled = true
                        } else {
                            madejasActuales.text = "Madejas actuales: -"
                            madejas.isEnabled = false
                            btnGuardar.isEnabled = false
                        }
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnGuardar.setOnClickListener {
            val codigoHilo = hilo.text.toString().trim().uppercase()
            val cantidad = madejas.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch(Dispatchers.IO) {
                val hiloStock = dao.obtenerPorHiloUsuario(codigoHilo, userId)
                if (hiloStock != null && cantidad > 0) {
                    val hiloActualizado = hiloStock.copy(madejas = hiloStock.madejas + cantidad)
                    dao.actualizarStock(hiloActualizado)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@StockPersonal, "Madejas sumadas correctamente", Toast.LENGTH_SHORT)
                            .show()
                        refrescarUI()
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    /**
     * Muestra un diálogo para restar madejas de un hilo existente:
     * - Al escribir el código, muestra las madejas actuales.
     * - El botón se habilita solo si el hilo existe.
     * - Al confirmar, decrementa el total hasta un mínimo de 0 madejas.
     */
    private fun dialogEliminarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val codigoHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloMadejasEliminarStk)
        val madejas = dialog.findViewById<EditText>(R.id.edTxt_edTxt_introducirNumMadejasEliminarStk)
        val madejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_eliminarMadejaConfirmarStk)
        dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)
            .setOnClickListener { dialog.dismiss() }

        var hiloStockEntity: HiloStockEntity? = null

        codigoHilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val numHilo = s.toString().trim().uppercase()
                lifecycleScope.launch(Dispatchers.IO) {
                    hiloStockEntity = dao.obtenerPorHiloUsuario(numHilo, userId)
                    withContext(Dispatchers.Main) {
                        if (hiloStockEntity != null) {
                            madejasActuales.text = "Madejas actuales: ${hiloStockEntity!!.madejas}"
                            btnGuardar.isEnabled = true
                        } else {
                            madejasActuales.text = "Madejas actuales: -"
                            btnGuardar.isEnabled = false
                        }
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnGuardar.setOnClickListener {
            val cantidadBorrar = madejas.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch(Dispatchers.IO) {
                hiloStockEntity?.let {
                    val nueva = maxOf(0, it.madejas - cantidadBorrar)
                    val hiloActualizado = it.copy(madejas = nueva)
                    dao.actualizarStock(hiloActualizado)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@StockPersonal,
                            "Madejas actualizadas correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        refrescarUI()
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    /**
     * Muestra un diálogo de confirmación para eliminar un hilo completo del stock:
     * - Resalta el ID del hilo en rojo dentro del mensaje.
     * - Al confirmar, elimina la entidad de la base de datos y refresca la UI.
     *
     * @param pos índice en [listaStock] del hilo a eliminar.
     */
    private fun dialogEliminarHilo(pos: Int) {
        val hilo = listaStock[pos]

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarHilo)
        val btnConfirmarBorrar = dialog.findViewById<TextView>(R.id.txtVw_confirmarEliminarHiloStk)

        /* construye el texto resaltado en rojo del ID del hilo */
        val plantilla = getString(R.string.confirmarEliminarHiloStk)
        val texto = plantilla.replace("%s", hilo.hiloId)
        val span = SpannableString(texto).apply {
            val start = texto.indexOf(hilo.hiloId)
            setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                start + hilo.hiloId.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        btnConfirmarBorrar.text = span

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnEliminar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.eliminarPorUsuarioYHilo(userId, hilo.hiloId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@StockPersonal,
                        "Hilo '${hilo.hiloId}' eliminado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    refrescarUI()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
