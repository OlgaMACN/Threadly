package logica.catalogo_hilos

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
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
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloCatalogoDao
import persistencia.entidades.HiloCatalogoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ValidarFormatoHilos
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

/**
 * Actividad que muestra y gestiona el catálogo de hilos del usuario.
 *
 * Permite al usuario:
 *  - Visualizar la lista de hilos disponibles en un RecyclerView.
 *  - Buscar hilos por número o nombre, con resaltado de coincidencias.
 *  - Añadir nuevos hilos al catálogo.
 *  - Modificar datos (número y/o nombre) de hilos existentes.
 *  - Eliminar hilos del catálogo con confirmación.
 *
 * Extiende de [BaseActivity] e incorpora:
 *  - Toolbar personalizado.
 *  - Gestión de sesión de usuario.
 *  - Operaciones de base de datos mediante DAOs y corrutinas.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class CatalogoHilos : BaseActivity() {

    /**
     * Lista interna de hilos, obtenida de la BD.
     */
    private var entidades: List<HiloCatalogoEntity> = emptyList()

    /**
     * Lista de objetos de dominio adaptados para mostrar en el RecyclerView.
     */
    private var listaCatalogo = mutableListOf<HiloCatalogo>()

    /**
     * RecyclerView que muestra el catálogo de hilos.
     */
    private lateinit var tablaCatalogo: RecyclerView

    /**
     * Adaptador para el RecyclerView de catálogo.
     * Recibe la lista y un callback para eliminar.
     */
    private lateinit var adaptadorCatalogo: AdaptadorCatalogo

    /**
     * DAO para acceder a la tabla de catálogo de hilos.
     */
    private lateinit var dao: HiloCatalogoDao

    /**
     * Identificador del usuario en sesión.
     */
    private var userId: Int = -1

    /**
     * Inicialización de la actividad.
     *
     * - Asigna layout y toolbar.
     * - Inicializa DAO y verifica sesión.
     * - Configura RecyclerView con adaptador.
     * - Carga inicialmente datos del catálogo.
     * - Enlaza botones para agregar y modificar hilos.
     * - Configura el buscador de catálogo.
     *
     * @param savedInstanceState Estado previo, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogo_aa_hilos)
        funcionToolbar(this)

        /* inicializar DAO y obtener sesión */
        dao = ThreadlyDatabase
            .getDatabase(applicationContext)
            .hiloCatalogoDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        /* configurar RecyclerView y adaptador */
        tablaCatalogo = findViewById(R.id.tabla_catalogo)
        adaptadorCatalogo = AdaptadorCatalogo(listaCatalogo, ::dialogEliminarHiloCatalogo)
        tablaCatalogo.layoutManager = LinearLayoutManager(this)
        tablaCatalogo.adapter = adaptadorCatalogo

        /* refrescar UI en hilo IO */
        lifecycleScope.launch(Dispatchers.IO) {
            refrescarUI()
        }

        /* botones de diálogo para agregar y modificar hilos */
        findViewById<Button>(R.id.btn_agregarHiloConsulta)
            .setOnClickListener { dialogAgregarHiloCatalogo() }
        findViewById<Button>(R.id.btn_modificarHiloConsulta)
            .setOnClickListener { dialogModificarHiloCatalogo() }

        /* llamar al buscador */
        buscadorCatalogo()
    }

    /**
     * Refresca la interfaz cargando los hilos de la BD y ordenándolos.
     *
     * - Obtiene lista de entidades del DAO.
     * - Transforma en lista de objetos de dominio.
     * - Ordena usando función utilitaria y actualiza el adaptador en Main.
     */
    private fun refrescarUI() {
        lifecycleScope.launch(Dispatchers.IO) {
            entidades = dao.obtenerHilosPorUsuario(userId)

            listaCatalogo = ordenarHilos(entidades.map { e ->
                HiloCatalogo(e.numHilo, e.nombreHilo, e.color)
            }) { it.numHilo }.toMutableList()

            withContext(Dispatchers.Main) {
                adaptadorCatalogo.actualizarLista(listaCatalogo)
            }
        }
    }

    /**
     * Configura la funcionalidad de búsqueda en el catálogo.
     *
     * - Oculta teclado al buscar.
     * - Permite búsqueda exacta por número o parcial por nombre.
     * - Resalta el hilo encontrado y sitúa en pantalla.
     * - Muestra mensaje cuando no hay resultados.
     * - Restablece vista al borrar texto.
     */
    private fun buscadorCatalogo() {
        val hiloBuscar = findViewById<EditText>(R.id.txtVw_buscarHiloConsulta)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaCatalogo)
        val sinResultados = findViewById<TextView>(R.id.txtVw_sinResultadosCatalogo)
        sinResultados.visibility = View.GONE

        /* búsqueda con el icono de lupa */
        btnLupa.setOnClickListener {
            /* ocultar teclado */
            val hideKB = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideKB.hideSoftInputFromWindow(hiloBuscar.windowToken, 0)

            val busqueda = hiloBuscar.text.toString().trim().uppercase()

            if (busqueda.isEmpty()) {
                /* restaurar lista completa */
                adaptadorCatalogo.resaltarHilo(null)
                adaptadorCatalogo.actualizarLista(listaCatalogo)
                tablaCatalogo.visibility = View.VISIBLE
                sinResultados.visibility = View.GONE
                return@setOnClickListener
            }

            /* encontrar primera coincidencia */
            val encontrado = listaCatalogo.find {
                it.numHilo == busqueda || it.nombreHilo.contains(busqueda, ignoreCase = true)
            }

            if (encontrado != null) {
                val idx = listaCatalogo.indexOf(encontrado)
                adaptadorCatalogo.resaltarHilo(encontrado.numHilo)
                adaptadorCatalogo.notifyDataSetChanged()
                tablaCatalogo.scrollToPosition(idx)
                tablaCatalogo.visibility = View.VISIBLE
                sinResultados.visibility = View.GONE
            } else {
                /* sin resultados */
                tablaCatalogo.visibility = View.GONE
                sinResultados.visibility = View.VISIBLE
            }
        }

        /* ecucha para restaurar al borrar texto */
        hiloBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorCatalogo.resaltarHilo(null)
                    adaptadorCatalogo.actualizarLista(listaCatalogo)
                    tablaCatalogo.visibility = View.VISIBLE
                    sinResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Muestra un diálogo para agregar un nuevo hilo al catálogo.
     *
     * - Valida campos no vacíos y formato de número de hilo.
     * - Verifica si ya existe antes de insertar.
     * - Inserta en BD y refresca UI.
     * - Feedback con Toasts.
     */
    private fun dialogAgregarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_agregar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val numeroHilo = dialog.findViewById<EditText>(R.id.edTxt_agregarNumHilo)
        val nombreHilo = dialog.findViewById<EditText>(R.id.editTxt_agregarNombreHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverAgregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarAgregarHilo)

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnGuardar.setOnClickListener {
            val num = numeroHilo.text.toString().uppercase().trim()
            val nom = nombreHilo.text.toString().trim()
            if (num.isEmpty() || nom.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!ValidarFormatoHilos.formatoValidoHilo(num)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val existente = dao.obtenerHiloPorNumYUsuario(num, userId)
                withContext(Dispatchers.Main) {
                    if (existente != null) {
                        Toast.makeText(
                            this@CatalogoHilos,
                            "Ya existe un hilo con ese número",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                       /* si no existe se inserta */
                        val ent = HiloCatalogoEntity(
                            userId = userId,
                            numHilo = num,
                            nombreHilo = nom
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            dao.insertarHilo(ent)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@CatalogoHilos,
                                    "Hilo añadido correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refrescarUI()
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    /**
     * Muestra un diálogo para seleccionar datos a modificar de un hilo existente.
     *
     * - Permite elegir modificar número, nombre o ambos.
     * - Valida selección y existencia del hilo.
     * - Navega al diálogo final de modificación.
     */
    private fun dialogModificarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val numeroHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHilo)
        val chkNum = dialog.findViewById<CheckBox>(R.id.ckBx_numHiloModificar)
        val chkNom = dialog.findViewById<CheckBox>(R.id.ckB_nombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHilo)
        val btnNext = dialog.findViewById<Button>(R.id.btn_botonSiguienteModificar)

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnNext.setOnClickListener {
            val numBusq = numeroHilo.text.toString().trim().uppercase()
            if (numBusq.isEmpty()) {
                Toast.makeText(this, "Introduce el número a modificar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!chkNum.isChecked && !chkNom.isChecked) {
                Toast.makeText(this, "Selecciona un campo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val entidad = dao.obtenerHiloPorNumYUsuario(numBusq, userId)
                withContext(Dispatchers.Main) {
                    if (entidad == null) {
                        Toast.makeText(this@CatalogoHilos, "No existe ese hilo", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        dialog.dismiss()
                        dialogModificarHiloFinal(entidad, chkNum.isChecked, chkNom.isChecked)
                    }
                }
            }
        }

        dialog.show()
    }

    /**
     * Diálogo final para modificar número y/o nombre de un hilo.
     *
     * - Muestra campos según selección previa.
     * - Valida no dejar campos vacíos y formato de número.
     * - Evita duplicados de número al modificar.
     * - Actualiza entidad en BD y refresca UI.
     *
     * @param entidadVieja Objeto original de la BD a modificar.
     * @param modNum Indica si se modifica el número.
     * @param modNom Indica si se modifica el nombre.
     */
    private fun dialogModificarHiloFinal(
        entidadVieja: HiloCatalogoEntity,
        modNum: Boolean,
        modNom: Boolean
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar_final)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val numeroHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloModificar)
        val nombreHilo = dialog.findViewById<EditText>(R.id.editTxt_introducirNombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHiloFinal)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarModificarHilo)

        if (modNum) numeroHilo.setText(entidadVieja.numHilo) else numeroHilo.visibility = View.GONE
        if (modNom) nombreHilo.setText(entidadVieja.nombreHilo) else nombreHilo.visibility = View.GONE

        btnVolver.setOnClickListener {
            dialog.dismiss()
            dialogModificarHiloCatalogo()
        }

        btnGuardar.setOnClickListener {
            val nuevoNum =
                if (modNum) numeroHilo.text.toString().uppercase().trim() else entidadVieja.numHilo
            val nuevoNom = if (modNom) nombreHilo.text.toString()
                .trim() else entidadVieja.nombreHilo

            if ((modNum && nuevoNum.isEmpty()) || (modNom && nuevoNom.isEmpty())) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (modNum && !ValidarFormatoHilos.formatoValidoHilo(nuevoNum)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                if (modNum && nuevoNum != entidadVieja.numHilo) {
                    val existente = dao.obtenerHiloPorNumYUsuario(nuevoNum, userId)
                    if (existente != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CatalogoHilos,
                                "Ya existe un hilo con ese número",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                }

                val entidadNueva = entidadVieja.copy(
                    numHilo = nuevoNum,
                    nombreHilo = nuevoNom
                )
                dao.actualizarHilo(entidadNueva)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CatalogoHilos, "Hilo modificado correctamente", Toast.LENGTH_SHORT).show()
                    refrescarUI()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    /**
     * Diálogo para confirmar y eliminar un hilo del catálogo.
     *
     * - Muestra mensaje con número de hilo resaltado en rojo.
     * - Gestiona acción de eliminar en BD.
     * - Refresca UI tras eliminación.
     *
     * @param hilo Objeto de dominio que contiene datos del hilo.
     */
    private fun dialogEliminarHiloCatalogo(hilo: HiloCatalogo) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_eliminar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloCat)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_botonVolverEliminarCat)
        val txtMsg = dialog.findViewById<TextView>(R.id.txtVw_mensajeEliminarHiloCat)

        /* preparar mensaje con número resaltado */
        val plantilla = getString(R.string.confirmarEliminarHiloCat)
        val texto = plantilla.replace("%s", hilo.numHilo)
        val span = SpannableString(texto)
        val inicio = texto.indexOf(hilo.numHilo)
        val final = inicio + hilo.numHilo.length
        span.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            inicio,
            final,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        txtMsg.text = span

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnEliminar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.eliminarPorNumYUsuario(hilo.numHilo, userId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CatalogoHilos,
                        "Hilo ${hilo.numHilo} eliminado correctamente",
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
