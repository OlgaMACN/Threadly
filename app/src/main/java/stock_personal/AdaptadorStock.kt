package stock_personal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorStock(
    private var items: List<HiloStock> = listOf(),
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<AdaptadorStock.StockViewHolder>() {

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_tabla_filas_contenido, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = items[position]
        holder.txtHilo.text = item.hiloId
        holder.txtMadejas.text = item.madejas.toString()
    }

    override fun getItemCount(): Int = items.size

    fun actualizarLista(nuevaLista: List<HiloStock>) {
        items = nuevaLista
        notifyDataSetChanged()
    }

    fun actualizarHilo(hiloActualizado: HiloStock) {
        val index = items.indexOfFirst { it.hiloId == hiloActualizado.hiloId }
        if (index != -1) {
            items = items.toMutableList().apply {
                set(index, hiloActualizado)
            }
            notifyItemChanged(index)
        }
    }
}
