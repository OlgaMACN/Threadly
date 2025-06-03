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
    private val onTotalChanged: (Int) -> Unit,
    private val onUpdateMadejas: (HiloGrafico) -> Unit
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

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

        holder.txtHilo.text = hiloItem.hilo
        holder.txtMadejas.text = hiloItem.madejas.toString()

        holder.textWatcher?.let {
            holder.edtModificar.removeTextChangedListener(it)
        }

        holder.edtModificar.setText(hiloItem.cantidadModificar?.toString() ?: "")

        holder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                hiloItem.cantidadModificar = s?.toString()?.toIntOrNull()
                onTotalChanged(calcularTotal())
                onUpdateMadejas(hiloItem)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.edtModificar.addTextChangedListener(holder.textWatcher)

        when {
            hiloItem.hilo == hiloResaltadoBusqueda -> {
                /* búsqueda */
                holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
            }
            hiloItem.hilo == hiloResaltadoClick -> {
                /* azulito stock */
                holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_hilografico_stock)
            }
            else -> {
                holder.filaLayout.setBackgroundResource(android.R.color.transparent)
            }
        }

        holder.txtHilo.setOnClickListener {
            onClickHilo(hiloItem)
        }

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
        hiloResaltadoClick = null
        notifyDataSetChanged()
    }

    /**
     * Resalta (color B) el [hiloId] porque se ha hecho clic para ver stock.
     * Se limpia cualquier resaltado por búsqueda anterior.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHiloClick(hiloId: String?) {
        hiloResaltadoBusqueda = null
        /* con toggle para clicar y desmarcar jejeje */
        hiloResaltadoClick = if (hiloResaltadoClick == hiloId) null else hiloId
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
    fun obtenerHiloResaltadoClick(): String? = hiloResaltadoClick

}
