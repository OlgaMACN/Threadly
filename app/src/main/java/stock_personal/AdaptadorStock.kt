package stock_personal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tabla_fila, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = items[position]
        holder.txtHilo.text = item.hiloId
        holder.txtMadejas.text = item.madejas.toString()
    }

    override fun getItemCount(): Int = items.size
}
