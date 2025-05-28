package logica.stock_personal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador para mostrar el inventario personal de hilos en un RecyclerView.
 * Cada fila muestra el ID del hilo y la cantidad de madejas que posee el usuario.
 * Permite gestionar eventos de pulsación larga y resaltar un hilo buscado.
 *
 * @param items Lista mutable de hilos en el stock personal.
 * @param onLongClick Función lambda que se ejecuta al mantener pulsado un elemento.
 * @param hiloResaltado ID del hilo que debe mostrarse resaltado en la tabla.
 *
 * * @author Olga y Sandra Macías Aragón
 */
class AdaptadorStock(
    private var items: MutableList<HiloStock> = mutableListOf(),
    private val onLongClick: (Int) -> Unit,
    private var hiloResaltado: String? = null /* para resaltar el hilo encontrado mediante búsqueda */
) : RecyclerView.Adapter<AdaptadorStock.StockViewHolder>() {

    /**
     * ViewHolder que contiene y gestiona los elementos visuales de cada fila.
     *
     * @param view Vista inflada que representa una fila de la tabla de stock.
     */
    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_hiloID)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_numeroMadejasTabla)
        val filaLayout: View = view /* contenedor completo de la fila (para cambiar fondo) */

        init {
            /* asigna el evento de pulsación larga a la fila */
            view.setOnLongClickListener {
                onLongClick(adapterPosition)
                true
            }
        }
    }

    /**
     * Crea un nuevo ViewHolder al inflar el layout correspondiente a una fila del RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_tabla_filas_contenido, parent, false)
        return StockViewHolder(view)
    }

    /**
     * Asocia los datos de un hilo con la vista de su fila correspondiente.
     *
     * @param holder ViewHolder a poblar con datos.
     * @param position Posición del elemento en la lista.
     */
    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = items[position]
        holder.txtHilo.text = item.hiloId
        holder.txtMadejas.text = item.madejas.toString()

        /* resalta la fila si coincide con el hilo buscado */
        if (item.hiloId == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }
    }

    /**
     * Devuelve el número total de elementos en la lista del adaptador.
     */
    override fun getItemCount(): Int = items.size

    /**
     * Actualiza toda la lista del adaptador con una nueva lista de hilos.
     * Por ahora se usa `notifyDataSetChanged()` de forma global.
     *
     * @param nuevaLista Nueva lista de hilos a mostrar.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloStock>) {
        items = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Actualiza un hilo específico en la lista, si existe, y notifica el cambio.
     *
     * @param hiloActualizado Hilo con nuevos datos que debe reemplazar al anterior.
     */
    fun actualizarHilo(hiloActualizado: HiloStock) {
        val index = items.indexOfFirst { it.hiloId == hiloActualizado.hiloId }
        if (index != -1) {
            items[index] = hiloActualizado
            notifyItemChanged(index)
        }
    }

    /**
     * Resalta visualmente un hilo específico en la tabla.
     * Si es null, se elimina cualquier resaltado previo.
     *
     * @param hiloId ID del hilo a resaltar o null para eliminar resaltado.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }
}
