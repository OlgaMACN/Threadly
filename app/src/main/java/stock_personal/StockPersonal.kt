package stock_personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlin.collections.mutableListOf
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class StockPersonal : AppCompatActivity() {

    private lateinit var tablaStock: RecyclerView
    private lateinit var adaptador: StockAdapter
    private val listaStock: mutableListOf<StockItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)

        tablaStock = findViewById(R.id.tabla_stock)
        adaptador = StockAdapter(listaStock, ::onItemLongClick)
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

        buscador.addTextChangedListener {
            val query = it.toString()
            val match = listaStock.find { item -> item.hiloId.contains(query, ignoreCase = true) }
            if (match != null) {
                val index = listaStock.indexOf(match)
                tablaStock.scrollToPosition(index)
            } else if (query.isNotEmpty()) {
                Toast.makeText(this, "No cuentas con ese hilo en tu stock personal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogAgregarHilo() {
        /* con layout inflater se pueden cargar layouts como vistas emergentes, perfecto para dialog */
        val dialogView = layoutInflater.inflate(R.layout.stock_dialog_agregar_hilo, null)
        val inputHilo = dialogView.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inputMadejas = dialogView.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)

        AlertDialog.Builder(this)
            .setTitle("Agregar nuevo hilo")
            .setView(dialogView)
            .setNegativeButton("Volver", null)
            .setPositiveButton("Guardar") { _, _ ->
                val hilo = inputHilo.text.toString().uppercase().trim()
                val madejas = inputMadejas.text.toString().toIntOrNull()

                if (hilo.isEmpty() || madejas == null || madejas < 0) {
                    Toast.makeText(this, "Sólo números enteros positivos o letras", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                if (listaStock.any { it.hiloId == hilo }) {
                    Toast.makeText(this, "El hilo '$hilo' ya existe", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                listaStock.add(StockItem(hilo, madejas))
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

// Modelo
data class StockItem(var hiloId: String, var madejas: Int)

// Adaptador básico para RecyclerView
class StockAdapter(
    private val items: List<StockItem>,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_hiloID)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_numeroMadejasTabla)

        init {
            view.setOnLongClickListener {
                onLongClick(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tabla_fila, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = items[position]
        holder.txtHilo.text = item.hiloId
        holder.txtMadejas.text = item.madejas.toString()
    }

    override fun getItemCount(): Int = items.size


}
