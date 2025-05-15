package CatalogoHilos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorCatalogo(
    private var items: MutableList<HiloCatalogo> = mutableListOf(), /* mutable porque la tabla es cambiante */
    private val onLongClick: (Int) -> Unit,
    private var hiloResaltado: String? = null /* para el hilo encontrado mediante el buscador */
) : RecyclerView.Adapter<AdaptadorCatalogo.CatalogoViewHolder>() {

    inner class CatalogoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtHilo: TextView = view.findViewById(R.id.txtVw_numHiloConsulta)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_nombreHiloConsulta)
        val filaLayout: View = view /* contenedor de la fila para cambiar el fondo */

        init {
            view.setOnLongClickListener {
                onLongClick(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdaptadorCatalogo.CatalogoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_tabla_filas_contenido, parent, false)
        return CatalogoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdaptadorCatalogo.CatalogoViewHolder, position: Int) {
        val item = items[position]
        holder.txtHilo.text = item.numHilo.toString()
        holder.txtMadejas.text = item.nombreHilo

        /* comprueba si ha de resaltar la fila o no */
        if (item.numHilo.toString() == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloCatalogo>) {
        items = nuevaLista.toMutableList()
        // TODO cambiar a algo más eficiente pero de momento tira
        notifyDataSetChanged()
    }

    fun actualizarHilo(hiloActualizado: HiloCatalogo) {
        val index = items.indexOfFirst { it.numHilo == hiloActualizado.numHilo }
        if (index != -1) {
            items[index] = hiloActualizado
            notifyItemChanged(index)
        }
    }

    /* actualizar el hilo resaltado en la tabla */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(numHilo: String?) {
        hiloResaltado = numHilo
        notifyDataSetChanged()
    }

}
