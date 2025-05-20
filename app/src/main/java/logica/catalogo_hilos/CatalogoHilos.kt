package logica.catalogo_hilos

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.BaseActivity
import utiles.ajustarDialog
import utiles.funcionToolbar

class CatalogoHilos : BaseActivity() {

    /* variable para guardar el hilo a modificar */
    private var numHiloAModificar: Int? = null

    private lateinit var tablaCatalogo: RecyclerView
    private lateinit var adaptadorCatalogo: AdaptadorCatalogo
    private val listaCatalogo = mutableListOf<HiloCatalogo>()

    private lateinit var checkBoxHilo: CheckBox
    private lateinit var checkBoxNombre: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogo_aa_hilos)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        tablaCatalogo = findViewById(R.id.tabla_catalogo)

        adaptadorCatalogo =
            logica.catalogo_hilos.AdaptadorCatalogo(listaCatalogo, ::dialogEliminarHiloCatalogo)

        tablaCatalogo.layoutManager = LinearLayoutManager(this)
        tablaCatalogo.adapter = adaptadorCatalogo


        /* componentes */
        val btn_AgregarHilo = findViewById<Button>(R.id.btn_agregarHiloConsulta)
        val btn_ModificarHilo = findViewById<Button>(R.id.btn_modificarHiloConsulta)
        checkBoxHilo = findViewById(R.id.ckBx_numHiloModificar)
        checkBoxNombre = findViewById(R.id.ckB_nombreHiloModificar)

        /* acciones */
        btn_AgregarHilo.setOnClickListener() { dialogAgregarHiloCatalogo() }
        btn_ModificarHilo.setOnClickListener() { dialogModificarHiloCatalogo() }
        buscadorHilo()
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

            val numHilo = numHiloString.toIntOrNull()
            if (numHilo == null || numHilo < 0) {
                Toast.makeText(this, "Introduce un número de hilo válido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (listaCatalogo.any { it.numHilo == numHilo }) {
                Toast.makeText(
                    this,
                    "El número de hilo ya existe en el catálogo",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            listaCatalogo.add(HiloCatalogo(numHilo, nombreHilo, null))

            adaptadorCatalogo.notifyItemInserted(listaCatalogo.size - 1)
            adaptadorCatalogo.actualizarLista(listaCatalogo)

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

        val edTxt_numHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHilo)
        val btnSiguiente = dialog.findViewById<Button>(R.id.btn_botonSiguienteModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHilo)

        btnVolver.setOnClickListener { dialog.dismiss() }

        btnSiguiente.setOnClickListener {
            val numHiloStr = edTxt_numHilo.text.toString().trim()
            val numHilo = numHiloStr.toIntOrNull()

            if (numHilo == null) {
                Toast.makeText(this, "Número inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val encontrado = listaCatalogo.find { it.numHilo == numHilo }
            if (encontrado == null) {
                Toast.makeText(this, "Hilo no encontrado", Toast.LENGTH_LONG).show()
            } else {
                numHiloAModificar = numHilo
                dialog.dismiss()
                dialogModificarHiloFinal()
            }
        }

        dialog.show()
    }

    /* modificar hilo 2: con los campos escogidos ya se procede a modificar */
    private fun dialogModificarHiloFinal() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar_final)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val edTxt_numNuevo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloModificar)
        val edTxt_nombreNuevo =
            dialog.findViewById<EditText>(R.id.editTxt_introducirNombreHiloModificar)
        val checkNum = dialog.findViewById<CheckBox>(R.id.ckBx_numHiloModificar)
        val checkNombre = dialog.findViewById<CheckBox>(R.id.ckB_nombreHiloModificar)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarModificarHilo)
        val btnCancelar = dialog.findViewById<Button>(R.id.btn_volverModificarHiloFinal)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val modNum = checkNum.isChecked
            val modNombre = checkNombre.isChecked

            if (!modNum && !modNombre) {
                Toast.makeText(this, "Selecciona al menos una opción", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val index = listaCatalogo.indexOfFirst { it.numHilo == numHiloAModificar }
            if (index == -1) {
                Toast.makeText(this, "El hilo no existe en el catálogo", Toast.LENGTH_LONG).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            val nuevoNum = edTxt_numNuevo.text.toString().trim().toIntOrNull()

            /* normalizar antes validarlo por si acaso */
            val nuevoNombreRaw = edTxt_nombreNuevo.text.toString()
            val nuevoNombre = nuevoNombreRaw.trim().uppercase()

            if (modNum) {
                if (nuevoNum == null) {
                    Toast.makeText(this, "Número inválido", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (nuevoNum != numHiloAModificar && listaCatalogo.any { it.numHilo == nuevoNum }) {
                    Toast.makeText(this, "Ese número de hilo ya existe", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            if (modNombre && nuevoNombre.isBlank()) {
                Toast.makeText(this, "Nombre inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val hiloOriginal = listaCatalogo[index]
            val hiloModificado = HiloCatalogo(
                nuevoNum ?: hiloOriginal.numHilo,
                nuevoNombre.ifBlank { hiloOriginal.nombreHilo },
                hiloOriginal.color /* el color no cambia */
            )

            listaCatalogo[index] = hiloModificado
            adaptadorCatalogo.actualizarLista(listaCatalogo)
            adaptadorCatalogo.resaltarHilo(hiloModificado.numHilo.toString())
            tablaCatalogo.scrollToPosition(index)
            dialog.dismiss()
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
        val nombreHilo = hilo.nombreHilo
        val numHilo = hilo.numHilo.toString()

        val textoOriginal = getString(R.string.confirmarEliminarHiloCat)
        val textoConDatos = textoOriginal.replace("%s", "$numHilo - $nombreHilo")

        val spannable = SpannableString(textoConDatos)
        val start = textoConDatos.indexOf(numHilo)
        val end = start + numHilo.length + nombreHilo.length + 3

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
            dialog.dismiss()
        }

        dialog.show()
    }

}