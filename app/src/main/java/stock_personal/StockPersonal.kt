package stock_personal

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class StockPersonal : AppCompatActivity() {

    private lateinit var tablaStock: RecyclerView
    private lateinit var adaptadorStock: AdaptadorStock
    private val listaStock = mutableListOf<HiloStock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)

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
        val buscadorHilo = findViewById<EditText>(R.id.edTxt_buscadorPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarHilo.setOnClickListener { dialogAgregarHilo() }
        btnAgregarMadeja.setOnClickListener { dialogAgregarMadeja() }
        btnEliminarMadeja.setOnClickListener { dialogEliminarMadeja() }

        /* acción del buscador */
        buscadorHilo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val busqueda = s.toString()
                val match =
                    listaStock.find { item -> item.hiloId.contains(busqueda, ignoreCase = true) }
                if (match != null) {
                    val index = listaStock.indexOf(match)
                    tablaStock.scrollToPosition(index)
                } else if (busqueda.isNotEmpty()) {
                    Toast.makeText(
                        this@StockPersonal,
                        "No cuentas con ese hilo en tu stock personal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun dialogAgregarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_hilo)

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
        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inputMadejas = dialog.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_agregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_botonAgregarHiloStk)

        btnVolver.setOnClickListener {
            dialog.dismiss() // se cierra el dialog
        }

        btnGuardar.setOnClickListener {
            val hilo = inputHilo.text.toString().uppercase().trim()
            val madejas = inputMadejas.text.toString().toIntOrNull()

            if (hilo.isEmpty() || madejas == null || madejas < 0) {
                Toast.makeText(this, "Solo números enteros positivos o letras", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            if (listaStock.any { it.hiloId == hilo }) {
                Toast.makeText(this, "El hilo '$hilo' ya existe", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            listaStock.add(HiloStock(hilo, madejas))
            adaptadorStock.notifyItemInserted(listaStock.size - 1)
            /* una vez insertado el hilo, se cierra el dialog*/
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun dialogAgregarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
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
                adaptadorStock.notifyItemChanged(listaStock.indexOf(item))
                dialog.dismiss()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun dialogEliminarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumMadejasEliminarStk)
        val txtMadejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_eliminarMadejaConfirmarStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)

        var hiloEncontrado: HiloStock? = null

        inputHilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hilo = s.toString().uppercase().trim()
                hiloEncontrado = listaStock.find { it.hiloId == hilo }

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
            val cantidadStr = inputHilo.text.toString().trim()
            val cantidad = cantidadStr.toIntOrNull()

            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            hiloEncontrado?.let { hilo ->
                hilo.madejas = maxOf(0, hilo.madejas - cantidad)
                adaptadorStock.notifyItemChanged(listaStock.indexOf(hilo))
                dialog.dismiss()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /* para borrar un hilo manteniendo pulsada la fila */
    private fun dialogEliminarHilo(posicion: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_hilo)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        /* variables del dialog */
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarHilo)

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            val hiloEliminado = listaStock[posicion].hiloId
            listaStock.removeAt(posicion)
            adaptadorStock.notifyItemRemoved(posicion)

            Toast.makeText(this, "Hilo '$hiloEliminado' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}

