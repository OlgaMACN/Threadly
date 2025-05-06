package stock_personal

import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import android.widget.Button
import android.widget.EditText
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
        adaptador = AdaptadorStock(listaStock, ::onItemLongClick)
        /* elementos de la tabla en vertical gracias al LinearLayoutManager */
        tablaStock.layoutManager = LinearLayoutManager(this)
        tablaStock.adapter = adaptador

        /* declaramos los botones */
        val btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloStk)
        val btnAgregarMadeja = findViewById<Button>(R.id.btn_agregarMadejaStk)
        val btnEliminarMadeja = findViewById<Button>(R.id.btn_eliminarMadejaStk)
        val buscador = findViewById<EditText>(R.id.edTxt_buscadorPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarHilo.setOnClickListener { mostrarDialogAgregarHilo() }
        btnAgregarMadeja.setOnClickListener { mostrarDialogModificarMadejas(true) }
        btnEliminarMadeja.setOnClickListener { mostrarDialogModificarMadejas(false) }

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

    private fun mostrarDialogAgregarHilo() {
        /* con layout inflater se pueden cargar layouts como vistas emergentes, perfecto para dialog */
        val dialogView = layoutInflater.inflate(R.layout.stock_dialog_agregar_hilo, null)
        val inputHilo = dialogView.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inputMadejas =
            dialogView.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)

        AlertDialog.Builder(this)
            .setTitle("Agregar nuevo hilo")
            .setView(dialogView)
            .setNegativeButton("Volver", null)
            .setPositiveButton("Guardar") { _, _ ->
                val hilo = inputHilo.text.toString().uppercase().trim()
                val madejas = inputMadejas.text.toString().toIntOrNull()

                if (hilo.isEmpty() || madejas == null || madejas < 0) {
                    Toast.makeText(
                        this,
                        "Sólo números enteros positivos o letras",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }
                /* it es una palabra clave implícita en Kotlin usada para referirse al parámetro único de una función lambda.*/
                if (listaStock.any { it.hiloId == hilo }) {
                    Toast.makeText(this, "El hilo '$hilo' ya existe", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                listaStock.add(HiloStock(hilo, madejas))
                adaptador.notifyDataSetChanged()
            }.show()
    }

    private fun mostrarDialogModificarMadejas(esAgregar: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.stock_dialog_agregar_madeja, null)
        val inputHilo = dialogView.findViewById<EditText>(R.id.edTxt_agregarMadejasStk_hilo)
        val inputCantidad = dialogView.findViewById<EditText>(R.id.edTxt_agregarMadejasStk)

        val title = if (esAgregar) "Agregar Madejas" else "Eliminar Madejas"
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setNegativeButton("Volver", null)
            .setPositiveButton("Guardar") { _, _ ->
                val hilo = inputHilo.text.toString().uppercase().trim()
                val cant = inputCantidad.text.toString().toIntOrNull()

                if (cant == null || cant < 0) {
                    Toast.makeText(this, "Solo se permiten enteros positivos", Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }

                val item = listaStock.find { it.hiloId == hilo }
                if (item == null) {
                    Toast.makeText(this, "El hilo no existe", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                item.madejas = if (esAgregar) item.madejas + cant else maxOf(0, item.madejas - cant)
                adaptador.notifyDataSetChanged()
            }.show()
    }

    /* para borrar un hilo manteniendo pulsado */
    private fun onItemLongClick(posicion: Int) {
        val item = listaStock[posicion]
        AlertDialog.Builder(this)
            .setTitle("Eliminar hilo")
            .setMessage("¿Deseas eliminar el hilo ${item.hiloId} del stock?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                listaStock.removeAt(posicion)
                adaptador.notifyDataSetChanged()
            }.show()
    }
}


