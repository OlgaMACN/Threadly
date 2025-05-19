package logica.stock_personal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorStock(
    private var items: MutableList<HiloStock> = mutableListOf(), /* mutable porque la tabla es cambiante */
    private val onLongClick: (Int) -> Unit,
    private var hiloResaltado: String? = null /* para el hilo encontrado mediante el buscador */
) : RecyclerView.Adapter<AdaptadorStock.StockViewHolder>() {

    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_hiloID)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_numeroMadejasTabla)
        val filaLayout: View = view /* contenedor de la fila para cambiar el fondo */

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

        /* comprueba si ha de resaltar la fila o no */
        if (item.hiloId == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloStock>) {
        items = nuevaLista.toMutableList()
        // TODO cambiar a algo más eficiente pero de momento tira
        notifyDataSetChanged()
    }

    fun actualizarHilo(hiloActualizado: HiloStock) {
        val index = items.indexOfFirst { it.hiloId == hiloActualizado.hiloId }
        if (index != -1) {
            items[index] = hiloActualizado
            notifyItemChanged(index)
        }
    }

    /* actualizar el hilo resaltado en la tabla */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }
}
