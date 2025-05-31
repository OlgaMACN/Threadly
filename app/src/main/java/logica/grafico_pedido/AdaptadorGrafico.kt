package logica.grafico_pedido

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador para mostrar y gestionar la lista de hilos de un gráfico en un RecyclerView.
 * Ahora mantiene dos tipos de resaltado:
 *   1) hiloResaltadoBusqueda: color A (para buscar)
 *   2) hiloResaltadoClick:   color B (para ver stock)
 *
 * @param hilos             Lista mutable de [HiloGrafico]
 * @param onClickHilo       Lambda que se invoca al hacer clic en el TextView de cada hilo.
 * @param onLongClickHilo   Lambda opcional para pulsación larga (ej. eliminar).
 * @param onTotalChanged    Lambda que recibe el total de madejas (Int), se llama cada vez que cambian las cantidades.
 */
class AdaptadorGrafico(
    private val hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (HiloGrafico) -> Unit,
    private val onLongClickHilo: ((HiloGrafico) -> Unit)? = null,
    private val onTotalChanged: (Int) -> Unit
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    // Hilos resaltados por búsqueda (search) y por clic (stock)
    private var hiloResaltadoBusqueda: String? = null
    private var hiloResaltadoClick: String? = null

    inner class HiloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_textoHiloGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasGrafico)
        val edtModificar: EditText = view.findViewById(R.id.txtVw_columnaModificarPedidoMadeja)
        val filaLayout: View = view
        var textWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiloViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedidob_tabla_filas_contenido_grafico, parent, false)
        return HiloViewHolder(vista)
    }

    override fun getItemCount(): Int = hilos.size

    override fun onBindViewHolder(holder: HiloViewHolder, position: Int) {
        val hiloItem = hilos[position]

        // 1) Mostrar el código y la cantidad original de madejas
        holder.txtHilo.text = hiloItem.hilo
        holder.txtMadejas.text = hiloItem.madejas.toString()

        // 2) Retirar TextWatcher previo
        holder.textWatcher?.let {
            holder.edtModificar.removeTextChangedListener(it)
        }
        // 3) Mostrar la cantidad modificada (si hay) o dejar vacío
        holder.edtModificar.setText(hiloItem.cantidadModificar?.toString() ?: "")

        // 4) Asignar nuevo TextWatcher para actualizar cantidadModificar y total
        holder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                hiloItem.cantidadModificar = s?.toString()?.toIntOrNull()
                onTotalChanged(calcularTotal())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.edtModificar.addTextChangedListener(holder.textWatcher)

        // 5) Decidir qué fondo aplicar:
        //    - Primero, si coincide con hiloResaltadoBusqueda → usamos drawable “resaltar búsqueda”
        //    - Si no, si coincide con hiloResaltadoClick → usamos drawable “resaltar clic”
        //    - En cualquier otro caso, transparente
        when {
            hiloItem.hilo == hiloResaltadoBusqueda -> {
                // Color/Drawable para la búsqueda
                holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
            }
            hiloItem.hilo == hiloResaltadoClick -> {
                // Color/Drawable para el clic (p.ej. un azulito)
                holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_hilografico_stock)
            }
            else -> {
                holder.filaLayout.setBackgroundResource(android.R.color.transparent)
            }
        }

        // 6) Clic simple sobre el TextView del hilo: invocamos onClickHilo
        holder.txtHilo.setOnClickListener {
            onClickHilo(hiloItem)
        }
        // 7) Pulsación larga en toda la fila para invocar onLongClickHilo
        holder.itemView.setOnLongClickListener {
            onLongClickHilo?.invoke(hiloItem)
            true
        }
    }

    /**
     * Actualiza la lista completa de hilos, notifica cambios y recalcula el total.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nueva: List<HiloGrafico>) {
        hilos.clear()
        hilos.addAll(nueva)
        notifyDataSetChanged()
        onTotalChanged(calcularTotal())
    }

    /**
     * Resalta (color A) el [hiloId] como resultado de una búsqueda.
     * Se limpia cualquier resaltado por clic anterior.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHiloBusqueda(hiloId: String?) {
        hiloResaltadoBusqueda = hiloId
        // Al buscar, borro cualquier resaltado por clic
        hiloResaltadoClick = null
        notifyDataSetChanged()
    }

    /**
     * Resalta (color B) el [hiloId] porque se ha hecho clic para ver stock.
     * Se limpia cualquier resaltado por búsqueda anterior.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHiloClick(hiloId: String?) {
        hiloResaltadoClick = hiloId
        // Al clicar, borro cualquier resaltado por búsqueda previa
        hiloResaltadoBusqueda = null
        notifyDataSetChanged()
    }

    /**
     * Devuelve la lista interna de HiloGrafico.
     */
    fun obtenerLista(): MutableList<HiloGrafico> = hilos

    /**
     * Calcula el total de madejas sumando cantidadModificar (si existe) o madejas.
     */
    private fun calcularTotal(): Int {
        return hilos.sumOf { hilo ->
            hilo.cantidadModificar ?: hilo.madejas
        }
    }
}
