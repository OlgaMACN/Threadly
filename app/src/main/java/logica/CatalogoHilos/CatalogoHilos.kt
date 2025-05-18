package logica.CatalogoHilos

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.funcionToolbar

class CatalogoHilos : AppCompatActivity() {

    private lateinit var tablaCatalogo: RecyclerView
    private lateinit var adaptadorCatalogo: AdaptadorCatalogo
    private val listaCatalogo = mutableListOf<HiloCatalogo>()

    /*me declaro dos variables para guardar estado de CheckBox*/
    private var valorCheckHilo : Boolean = false
    private var valorCheckNombre : Boolean = false

    private lateinit var checkBoxHilo: CheckBox
    private lateinit var checkBoxNombre: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogo_aa_hilos)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */
        setContentView(R.layout.catalogo_aa_hilos)

        tablaCatalogo = findViewById(R.id.tabla_catalogo)

        adaptadorCatalogo = AdaptadorCatalogo(listaCatalogo, ::dialogEliminarHilo)

        tablaCatalogo.layoutManager = LinearLayoutManager(this)
        tablaCatalogo.adapter = adaptadorCatalogo


        val btn_AgregarHilo = findViewById<Button>(R.id.btn_agregarHiloConsulta)

        val btn_ModificarHilo = findViewById<Button>(R.id.btn_modificarHiloConsulta)


        //configuracion boton agregar hilo
        btn_AgregarHilo.setOnClickListener() { dialogAgregarHilo() }

        //configuracion boton modificar hilo
        btn_ModificarHilo.setOnClickListener() { dialogModificarHilo() }

        buscadorHilo()
    }

    /* acción del buscador */
    private fun buscadorHilo() {
        val editTextBuscar = findViewById<EditText>(R.id.txtVw_buscarHiloConsulta)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaCatalogo)
        val tablaStock = findViewById<RecyclerView>(R.id.tabla_catalogo)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultados)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val texto = editTextBuscar.text.toString().trim().uppercase()
            val coincidencia = listaCatalogo.find { it.numHilo.toString() == texto }

            if (coincidencia != null) {
                val resultados = listOf(coincidencia)
                /* si encuentra el hilo lo resaltará en la tabla */
                adaptadorCatalogo.resaltarHilo(coincidencia.numHilo.toString())
                adaptadorCatalogo.actualizarLista(listaCatalogo)
                tablaStock.visibility = View.VISIBLE
                txtNoResultados.visibility = View.GONE

                val index = listaCatalogo.indexOf(coincidencia)
                tablaStock.scrollToPosition(index)
            } else {
                tablaStock.visibility = View.GONE
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        /* si se borra la búsqueda la tabla vuelve a aparecer */
        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorCatalogo.resaltarHilo(null)
                    adaptadorCatalogo.actualizarLista(listaCatalogo)
                    tablaStock.visibility = View.VISIBLE
                    txtNoResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun dialogAgregarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_agregar_hilo)

        /* se oscurece el fondo y queda súper chulo */
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* ancho y alto para configurar el tamaño independientemente del layout */
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        /* con setCancelable se consigue que no se cierre el dialogo si el user clica fuera de él */
        dialog.setCancelable(false)

        /* variables para este dialog */
        val inputNumHilo = dialog.findViewById<EditText>(R.id.edTxt_agregarNumHilo)
        val inputNombreHilo =
            dialog.findViewById<EditText>(R.id.editTxt_agregarNombreHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverAgregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarAgregarHilo)


        btnGuardar.setOnClickListener {
            val hilo = inputNumHilo.text.toString().trim()
            val nombreHilo = inputNombreHilo.text.toString().uppercase().trim()

            if (hilo.isEmpty() || nombreHilo.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val numHilo =
                hilo.toIntOrNull() /* convierto a entero para poder validar datos numéricos */
            if (nombreHilo.isEmpty() || numHilo == null || numHilo < 0) {
                Toast.makeText(
                    this,
                    "Solo números enteros positivos o letras",
                    Toast.LENGTH_LONG
                )
                    .show()
                return@setOnClickListener
            }

            if (listaCatalogo.any { it.numHilo.toString() == hilo }) {
                Toast.makeText(this, "El hilo '$hilo' ya existe", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            listaCatalogo.add(HiloCatalogo(numHilo, nombreHilo))
            adaptadorCatalogo.notifyItemInserted(listaCatalogo.size - 1)
            adaptadorCatalogo.actualizarLista(listaCatalogo)
            /* una vez insertado el hilo, se cierra el dialog*/
            dialog.dismiss()
        }

        btnVolver.setOnClickListener() {

            dialog.dismiss()
        }

        dialog.show()
    }

    /*para escoger datos a modificar del hilo del catalogo*/
    private fun dialogModificarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar)

        /* se oscurece el fondo y queda súper chulo */
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* ancho y alto para configurar el tamaño independientemente del layout */
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        /* con setCancelable se consigue que no se cierre el dialogo si el user clica fuera de él */
        dialog.setCancelable(false)

        val inputNumHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHilo)
        checkBoxHilo = dialog.findViewById(R.id.ckBx_numHiloModificar)
        checkBoxNombre = dialog.findViewById(R.id.ckB_nombreHiloModificar)

        val btnSiguiente = dialog.findViewById<Button>(R.id.btn_botonSiguienteModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHilo)

        btnSiguiente.setOnClickListener() {
            //TODO asociar numero de hilo que se introduce aqui con siguiente metodo en caso de modificarlo (porque cambiaría)
            val hilo = inputNumHilo.text.toString().trim()

            if (hilo.isEmpty() || !checkBoxHilo.isChecked || !checkBoxNombre.isChecked) {

                Toast.makeText(this,
                    "Debes introducir un número de hilo y marcar una de las opciones", Toast.LENGTH_LONG).show()
            } else {

                guardarEstados()
                dialogModificarHiloFinal()
            }

        }

        btnVolver.setOnClickListener() {

            dialog.dismiss()
        }

        dialog.show()

    }

    /*guardamos estados de checkbox*/
    private fun guardarEstados() {
        valorCheckHilo = checkBoxHilo.isChecked
        valorCheckNombre = checkBoxNombre.isChecked
    }

    /*para modificar campo escogido del hilo correspondiente*/
    //TODO el metodo debe recibir por parametro el valor de cada uno de los checkbox
    private fun dialogModificarHiloFinal() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar_final)

        /* se oscurece el fondo y queda súper chulo */
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* ancho y alto para configurar el tamaño independientemente del layout */
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        /* con setCancelable se consigue que no se cierre el dialogo si el user clica fuera de él */
        dialog.setCancelable(false)

        val inputNumHiloNuevo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloModificar)
        val inputNombreHiloNuevo = dialog.findViewById<EditText>(R.id.editTxt_introducirNombreHiloModificar)

        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarModificarHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHiloFinal)

        btnGuardar.setOnClickListener() {

            when {

                valorCheckHilo && valorCheckNombre -> {
                    //TODO si ambos estan marcados, se pueden editar los dos
                }
                valorCheckHilo -> {
                    //TODO unicamente se ha marcado el num de hilo
                }
                valorCheckNombre -> {
                    //TODO unicamente se ha marcado el nombre
                }
            }

        }

        btnVolver.setOnClickListener() {

            dialog.dismiss()
        }

        dialog.show()

    }

    /* para borrar un hilo manteniendo pulsada la fila */
    private fun dialogEliminarHilo(posicion: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_eliminar_hilo)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        /* variables del dialog */
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloCat)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_botonVolverEliminarCat)

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            val hiloEliminado = listaCatalogo[posicion].numHilo
            listaCatalogo.removeAt(posicion)
            adaptadorCatalogo.notifyItemRemoved(posicion)

            Toast.makeText(this, "Hilo '$hiloEliminado' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}