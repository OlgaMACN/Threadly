package logica.catalogo_hilos

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.launch
import persistencia.bbdd.CatalogoBdD
import persistencia.dao.CatalogoDAO
import persistencia.entidades.Catalogo
import utiles.BaseActivity
import utiles.RepositorioCatalogo
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.toCatalogo

class CatalogoHilos : BaseActivity() {

    private lateinit var catalogoDao: CatalogoDAO

    private lateinit var tablaCatalogo: RecyclerView
    private lateinit var adaptadorCatalogo: AdaptadorCatalogo
    private val listaCatalogo = mutableListOf<HiloCatalogo>()

    private lateinit var checkBoxHilo: CheckBox
    private lateinit var checkBoxNombre: CheckBox

    /* añadir repositorio para acceder a la BBDD del catálogo */
    private lateinit var repositorioCatalogo: RepositorioCatalogo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogo_aa_hilos)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        tablaCatalogo = findViewById(R.id.tabla_catalogo)
        adaptadorCatalogo =
            AdaptadorCatalogo(listaCatalogo, ::dialogEliminarHiloCatalogo)
        tablaCatalogo.layoutManager = LinearLayoutManager(this)
        tablaCatalogo.adapter = adaptadorCatalogo

        /* inicialización del repositorio del catálogo y del dao */
        repositorioCatalogo = RepositorioCatalogo(this)
        catalogoDao = CatalogoBdD.getDatabase(applicationContext).catalogoDao()


        /* acceder a la BdD sin bloquear la interfaz y evitar fugas de memoria */
        lifecycleScope.launch {
            /* comprobará si es la primera vez en la propia función */
            repositorioCatalogo.inicializarCatalogoSiEsNecesario()
            val datos = repositorioCatalogo.obtenerCatalogo()


           // Log.d("CatalogoHilos", "Datos obtenidos: ${datos.size}") // <-- todo Añade esto

            listaCatalogo.clear()
            listaCatalogo.addAll(datos.map { it.toHiloCatalogo() })
            adaptadorCatalogo.actualizarLista(listaCatalogo)
        }

        /* componentes */
        val btn_AgregarHilo = findViewById<Button>(R.id.btn_agregarHiloConsulta)
        val btn_ModificarHilo = findViewById<Button>(R.id.btn_modificarHiloConsulta)
        checkBoxHilo = findViewById(R.id.ckBx_numHiloModificar)
        checkBoxNombre = findViewById(R.id.ckB_nombreHiloModificar)

        /* acciones */
        btn_AgregarHilo.setOnClickListener() { dialogAgregarHiloCatalogo() }
        btn_ModificarHilo.setOnClickListener {
            dialogModificarHiloCatalogo()
        }

        buscadorHilo()
    }

    /* función de conversión para poder usar el modelo del adaptador */
    fun Catalogo.toHiloCatalogo(): HiloCatalogo {
        return HiloCatalogo(
            numHilo = this.codigoHilo,
            nombreHilo = this.nombreHilo,
            color = this.color
        )
    }

    /* buscar un hilo en el catálogo */
    private fun buscadorHilo() {
        val editTextBuscar = findViewById<EditText>(R.id.txtVw_buscarHiloConsulta)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaCatalogo)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosCatalogo)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val texto = editTextBuscar.text.toString().trim().uppercase()
            val coincidencia = listaCatalogo.find {
                it.numHilo.toString() == texto || it.nombreHilo.contains(texto, ignoreCase = true)
            }

            if (coincidencia != null) {
                val index = listaCatalogo.indexOf(coincidencia)

                adaptadorCatalogo.resaltarHilo(coincidencia.numHilo.toString())
                adaptadorCatalogo.actualizarLista(listaCatalogo)
                tablaCatalogo.scrollToPosition(index)

                tablaCatalogo.visibility = View.VISIBLE
                txtNoResultados.visibility = View.GONE
            } else {
                tablaCatalogo.visibility = View.GONE
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorCatalogo.resaltarHilo(null)
                    adaptadorCatalogo.actualizarLista(listaCatalogo)
                    tablaCatalogo.visibility = View.VISIBLE
                    txtNoResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /* agregar hilo al catálogo */
    private fun dialogAgregarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_agregar_hilo)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inputNumHilo = dialog.findViewById<EditText>(R.id.edTxt_agregarNumHilo)
        val inputNombreHilo = dialog.findViewById<EditText>(R.id.editTxt_agregarNombreHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverAgregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarAgregarHilo)

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val numHiloString = inputNumHilo.text.toString().trim()
            val nombreHilo = inputNombreHilo.text.toString().uppercase().trim()

            if (numHiloString.isEmpty() || nombreHilo.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (listaCatalogo.any { it.numHilo == numHiloString }) {
                Toast.makeText(
                    this,
                    "El número de hilo ya existe en el catálogo",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val nuevoHilo = HiloCatalogo(numHiloString, nombreHilo, null)
            listaCatalogo.add(nuevoHilo)
            adaptadorCatalogo.notifyItemInserted(listaCatalogo.size - 1)
            adaptadorCatalogo.actualizarLista(listaCatalogo)

            lifecycleScope.launch {
                try {
                    catalogoDao.insertar(nuevoHilo.toCatalogo())

                    /* si el hilo se inserta bien en la BdD, se actualizan el adaptador y la lista */
                    listaCatalogo.add(nuevoHilo)
                    adaptadorCatalogo.actualizarLista(listaCatalogo)

                    runOnUiThread { /* mostrar toast en el hilo principal para que no crashee */
                        Toast.makeText(
                            this@CatalogoHilos,
                            "Hilo añadido al catálogo correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CatalogoHilos,
                            "Error al añadir el hilo :(",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    /* modificar hilo 1: se escoge el hilo y los campos a modificar */
    private fun dialogModificarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inputNumHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHilo)
        val checkNumHilo = dialog.findViewById<CheckBox>(R.id.ckBx_numHiloModificar)
        val checkNombreHilo = dialog.findViewById<CheckBox>(R.id.ckB_nombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHilo)
        val btnSiguiente = dialog.findViewById<Button>(R.id.btn_botonSiguienteModificar)

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        btnSiguiente.setOnClickListener {
            val numHiloTexto = inputNumHilo.text.toString().trim()
            val modificarNum = checkNumHilo.isChecked
            val modificarNombre = checkNombreHilo.isChecked

            if (numHiloTexto.isEmpty()) {
                Toast.makeText(this, "Introduce el número del hilo a modificar", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val posicion = listaCatalogo.indexOfFirst { it.numHilo == numHiloTexto }
            if (posicion == -1) {
                Toast.makeText(this, "No existe ese hilo en el catálogo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!modificarNum && !modificarNombre) {
                Toast.makeText(
                    this,
                    "Selecciona al menos un campo para modificar",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            dialogModificarHiloFinal(posicion, modificarNum, modificarNombre)
        }

        dialog.show()
    }

    /* modificar hilo 2: con los campos escogidos ya se procede a modificar */
    private fun dialogModificarHiloFinal(
        posicion: Int,
        modificarNum: Boolean,
        modificarNombre: Boolean
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar_final)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inputNumHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloModificar)
        val inputNombreHilo =
            dialog.findViewById<EditText>(R.id.editTxt_introducirNombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHiloFinal)
        val btnModificar = dialog.findViewById<Button>(R.id.btn_guardarModificarHilo)

        /* ocultar campos si el usuario sólo ha escogido uno */
        inputNumHilo.visibility = if (modificarNum) View.VISIBLE else View.GONE
        inputNombreHilo.visibility = if (modificarNombre) View.VISIBLE else View.GONE

        /* datos actuales del hilo */
        val hiloActual = listaCatalogo[posicion]
        if (modificarNum) inputNumHilo.setText(hiloActual.numHilo)
        if (modificarNombre) inputNombreHilo.setText(hiloActual.nombreHilo)

        btnVolver.setOnClickListener {
            dialog.dismiss()
            dialogModificarHiloCatalogo() /* volver a la selección inicial por si quiere cambiar algo */
        }

        btnModificar.setOnClickListener {
            if (modificarNum) {
                val nuevoNum = inputNumHilo.text.toString().trim()
                if (nuevoNum.isEmpty()) {
                    Toast.makeText(
                        this,
                        "El número de hilo no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                /* validar que el número nuevo no entre en conflicto con otro que ya exista */
                val existe = listaCatalogo.withIndex().any { (index, hilo) ->
                    hilo.numHilo == nuevoNum && index != posicion
                }
                if (existe) {
                    Toast.makeText(this, "Ya existe un hilo con ese número", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                hiloActual.numHilo = nuevoNum
            }

            if (modificarNombre) {
                val nuevoNombre = inputNombreHilo.text.toString().trim()
                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(
                        this,
                        "El nombre de hilo no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                hiloActual.nombreHilo = nuevoNombre
            }
            /* para que el adaptador sepa que el hilo ha cambiado */
            adaptadorCatalogo.actualizarLista(listaCatalogo)
            Toast.makeText(this, "Hilo modificado correctamente", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            lifecycleScope.launch {
                catalogoDao.actualizar(hiloActual.toCatalogo())
            }
        }

        dialog.show()
    }

    /* eliminar un hilo del catalogo */
    private fun dialogEliminarHiloCatalogo(posicion: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_eliminar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloCat)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_botonVolverEliminarCat)
        val txtConfirmacion = dialog.findViewById<TextView>(R.id.txtVw_mensajeEliminarHiloCat)

        val hilo = listaCatalogo[posicion]
        val numHilo = hilo.numHilo.toString()

        val textoOriginal = getString(R.string.confirmarEliminarHiloCat)
        val textoConDatos = textoOriginal.replace("%s", numHilo)

        val spannable = SpannableString(textoConDatos)
        val start = textoConDatos.indexOf(numHilo)
        val end = start + numHilo.length

        if (start != -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        txtConfirmacion.text = spannable

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            listaCatalogo.removeAt(posicion)
            adaptadorCatalogo.notifyItemRemoved(posicion)
            Toast.makeText(this, "Hilo $numHilo eliminado del catálogo", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                catalogoDao.eliminar(hilo.toCatalogo())
            }
            dialog.dismiss()
        }

        dialog.show()
    }

}