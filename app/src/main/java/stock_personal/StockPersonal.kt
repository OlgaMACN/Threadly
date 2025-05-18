package stock_personal

import android.annotation.SuppressLint
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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import ui_utils.ajustarDialog
import utiles.funcionToolbar

class StockPersonal : AppCompatActivity() {

    private lateinit var tablaStock: RecyclerView
    private lateinit var adaptadorStock: AdaptadorStock
    private val listaStock =
        StockSingleton.listaStock /* modificaciones guardadas en el singleton */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)

        /* llamada a la función para usar el toolbar */
        funcionToolbar(this)

        tablaStock = findViewById(R.id.tabla_stock)
        /* callback: pasa la función de eliminar hilo directamente al adaptador, es decir, la tabla */
        adaptadorStock = AdaptadorStock(listaStock, ::dialogEliminarHilo)
        /* elementos de la tabla en vertical gracias al LinearLayoutManager */
        tablaStock.layoutManager = LinearLayoutManager(this)
        tablaStock.adapter = adaptadorStock

        /* declaracion botones */
        val btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloStk)
        val btnAgregarMadeja = findViewById<Button>(R.id.btn_agregarMadejaStk)
        val btnEliminarMadeja = findViewById<Button>(R.id.btn_eliminarMadejaStk)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarHilo.setOnClickListener { dialogAgregarHilo() }
        btnAgregarMadeja.setOnClickListener { dialogAgregarMadeja() }
        btnEliminarMadeja.setOnClickListener { dialogEliminarMadeja() }

        /* métodos en llamada continua */
        buscadorHilo()
    }

    /* acción del buscador */
    private fun buscadorHilo() {
        val editTextBuscar = findViewById<EditText>(R.id.edTxt_buscadorHilo)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaStock)
        val tablaStock = findViewById<RecyclerView>(R.id.tabla_stock)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultados)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val texto = editTextBuscar.text.toString().trim().uppercase()
            val coincidencia = listaStock.find { it.hiloId == texto }

            if (coincidencia != null) {
                val resultados = listOf(coincidencia)
                /* si encuentra el hilo lo resaltará en la tabla */
                adaptadorStock.resaltarHilo(coincidencia.hiloId)
                adaptadorStock.actualizarLista(listaStock)
                tablaStock.visibility = View.VISIBLE
                txtNoResultados.visibility = View.GONE

                val index = listaStock.indexOf(coincidencia)
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
                    adaptadorStock.resaltarHilo(null)
                    adaptadorStock.actualizarLista(listaStock)
                    tablaStock.visibility = View.VISIBLE
                    txtNoResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /* agregar hilo al stock personal */
    private fun dialogAgregarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_hilo)

        /* se oscurece el fondo y queda súper chulo */
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        /* con setCancelable se consigue que no se cierre el dialogo si el user clica fuera de él */
        dialog.setCancelable(false)

        /* variables para este dialog */
        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inputMadejas =
            dialog.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_agregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_botonAgregarHiloStk)

        btnVolver.setOnClickListener {
            dialog.dismiss() /* se cierra el dialog */
        }

        btnGuardar.setOnClickListener {
            val hilo = inputHilo.text.toString().uppercase().trim()
            val madejasString = inputMadejas.text.toString().trim()

            if (hilo.isEmpty() || madejasString.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val madejas =
                madejasString.toIntOrNull() /* convierto a entero para poder validar datos numéricos */
            if (hilo.isEmpty() || madejas == null || madejas < 0) {
                Toast.makeText(
                    this,
                    "Solo números enteros positivos o letras",
                    Toast.LENGTH_LONG
                )
                    .show()
                return@setOnClickListener
            }

            if (listaStock.any { it.hiloId == hilo }) {
                Toast.makeText(this, "El hilo '$hilo' ya existe", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            listaStock.add(HiloStock(hilo, madejas))
            adaptadorStock.notifyItemInserted(listaStock.size - 1)
            adaptadorStock.actualizarLista(listaStock)
            /* una vez insertado el hilo, se cierra el dialog*/
            dialog.dismiss()
        }

        dialog.show()
    }

    /* agregar madeja al stock personal */
    private fun dialogAgregarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk_hilo)
        val inputCantidad = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk)
        val txtMadejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarMadejaStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)

        inputCantidad.isEnabled = false
        btnGuardar.isEnabled = false


        inputHilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hilo = s.toString().uppercase().trim()
                val item = listaStock.find { it.hiloId == hilo }

                if (item != null) {
                    txtMadejasActuales.text = "Madejas actuales: ${item.madejas}"
                    inputCantidad.isEnabled = true
                    btnGuardar.isEnabled = true
                } else {
                    txtMadejasActuales.text = "Madejas actuales: -"
                    inputCantidad.isEnabled = false
                    btnGuardar.isEnabled = false
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnGuardar.setOnClickListener {
            val hilo = inputHilo.text.toString().uppercase().trim()
            val cantidad = inputCantidad.text.toString().toIntOrNull()

            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = listaStock.find { it.hiloId == hilo }
            if (item != null) {
                item.madejas += cantidad
                adaptadorStock.actualizarHilo(item)
                dialog.dismiss()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /* eliminar madeja del stock personal */
    private fun dialogEliminarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        val idHilo =
            dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloMadejasEliminarStk)
        val cantidadEliminar =
            dialog.findViewById<EditText>(R.id.edTxt_edTxt_introducirNumMadejasEliminarStk)
        val txtMadejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_eliminarMadejaConfirmarStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)

        var hiloEncontrado: HiloStock? = null

        idHilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hiloId = s.toString().uppercase().trim()
                hiloEncontrado = listaStock.find { it.hiloId == hiloId }

                if (hiloEncontrado != null) {
                    txtMadejasActuales.text = "Madejas actuales: ${hiloEncontrado!!.madejas}"
                    btnEliminar.isEnabled = true
                } else {
                    txtMadejasActuales.text = "Madejas actuales: -"
                    btnEliminar.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnEliminar.setOnClickListener {
            val cantidadStr = cantidadEliminar.text.toString().trim()
            val cantidad = cantidadStr.toIntOrNull()

            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (hiloEncontrado != null) {
                val index = listaStock.indexOf(hiloEncontrado)
                hiloEncontrado!!.madejas = maxOf(0, hiloEncontrado!!.madejas - cantidad)
                adaptadorStock.actualizarHilo(hiloEncontrado!!)
                dialog.dismiss()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /* eliminar un hilo del stock personall manteniendo pulsada la fila */
    private fun dialogEliminarHilo(posicion: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_hilo)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        /* variables del dialog */
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarHilo)
        val hiloABorrar = dialog.findViewById<TextView>(R.id.txtVw_confirmarEliminarHiloStk)

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        /* para poder señalar el hilo que se va a borrar hay que capturarlo */
        val hiloEliminado = listaStock[posicion].hiloId

        /* obtener el texto original con el marcador "%s", mismo que en @strings */
        val textoOriginal = getString(R.string.confirmarEliminarHiloStk)

        /* se reemplaza el marcador "%s" con el hiloEliminado */
        val textoConHilo = textoOriginal.replace("%s", hiloEliminado)

        /* spannableString a partir del texto con el hilo */
        val spannable = SpannableString(textoConHilo)

        /*** encontrar la posición del hilo concreto dentro del texto */
        val start = textoConHilo.indexOf(hiloEliminado)
        val end = start + hiloEliminado.length

        /* y ponerlo en rojo */
        if (start != -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        hiloABorrar.text = spannable

        btnEliminar.setOnClickListener {
            listaStock.removeAt(posicion)
            adaptadorStock.notifyItemRemoved(posicion)

            Toast.makeText(this, "Hilo '$hiloEliminado' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

}

