package stock_personal

import android.app.Dialog
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class StockPersonal : AppCompatActivity() {

    private lateinit var tablaStock: RecyclerView
    private lateinit var adaptador: AdaptadorStock
    private val listaStock = mutableListOf<HiloStock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)

        tablaStock = findViewById(R.id.tabla_stock)
        adaptador = AdaptadorStock(listaStock, ::borrarHilo)
        /* elementos de la tabla en vertical gracias al LinearLayoutManager */
        tablaStock.layoutManager = LinearLayoutManager(this)
        tablaStock.adapter = adaptador

        /* declaracion botones */
        val btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloStk)
        val btnAgregarMadeja = findViewById<Button>(R.id.btn_agregarMadejaStk)
        val btnEliminarMadeja = findViewById<Button>(R.id.btn_eliminarMadejaStk)
        val buscador = findViewById<EditText>(R.id.edTxt_buscadorPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarHilo.setOnClickListener { dialogAgregarHilo() }
        btnAgregarMadeja.setOnClickListener { dialogAgregarEliminarMadejas(true) }
        btnEliminarMadeja.setOnClickListener { dialogAgregarEliminarMadejas(false) }

        /* acción del buscador */
        buscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val match =
                    listaStock.find { item -> item.hiloId.contains(query, ignoreCase = true) }
                if (match != null) {
                    val index = listaStock.indexOf(match)
                    tablaStock.scrollToPosition(index)
                } else if (query.isNotEmpty()) {
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

        // Fondo oscuro (imprescindible para que se vea el fondo desenfocado)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Opcional: Ancho y alto personalizados si quieres controlar el tamaño
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Puedes centrarlo verticalmente (ya lo hará por defecto si usas Dialog)
        dialog.setCancelable(true)

        // Acceder a los campos de entrada
        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inputMadejas = dialog.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_agregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_botonAgregarHiloStk)

        btnVolver.setOnClickListener {
            dialog.dismiss()
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
            adaptador.notifyItemInserted(listaStock.size - 1)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun dialogAgregarEliminarMadejas(esAgregar: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_madeja)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk_hilo)
        val inputCantidad = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk)
        val txtMadejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarMadejaStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)

        inputCantidad.isEnabled = false
        btnGuardar.isEnabled = false

        inputHilo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hilo = s.toString().uppercase().trim()
                val item = listaStock.find { it.hiloId == hilo }

                if (item != null) {
                    txtMadejasActuales.text = "Madejas actuales: ${item.madejas}"
                    inputCantidad.isEnabled = true
                    btnGuardar.isEnabled = true
                } else if (hilo.isNotEmpty()) {
                    txtMadejasActuales.text = "Madejas actuales: -"
                    inputCantidad.isEnabled = false
                    btnGuardar.isEnabled = false
                    Toast.makeText(this@StockPersonal, "El hilo no existe", Toast.LENGTH_SHORT).show()
                } else {
                    txtMadejasActuales.text = "Madejas actuales:"
                    inputCantidad.isEnabled = false
                    btnGuardar.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnGuardar.setOnClickListener {
            val hilo = inputHilo.text.toString().uppercase().trim()
            val cantidad = inputCantidad.text.toString().toIntOrNull()

            if (hilo.isEmpty() || cantidad == null || cantidad < 0) {
                Toast.makeText(this, "Revisa los datos ingresados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = listaStock.find { it.hiloId == hilo }
            if (item == null) {
                Toast.makeText(this, "El hilo no existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            item.madejas = if (esAgregar) item.madejas + cantidad else maxOf(0, item.madejas - cantidad)
            adaptador.notifyItemChanged(listaStock.indexOf(item))
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /* para borrar un hilo manteniendo pulsado */
    private fun borrarHilo(posicion: Int) {
        val item = listaStock[posicion]
        AlertDialog.Builder(this)
            .setTitle("Eliminar hilo")
            .setMessage("¿Deseas eliminar el hilo ${item.hiloId} del stock?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                listaStock.removeAt(posicion)
                adaptador.notifyItemRemoved(posicion)
            }.show()
    }
}


