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
 * @param items Lista mutable de objetos [HiloStock] representando el stock.
 * @param onEliminarClick Lambda que se ejecuta cuando se mantiene pulsada una fila.
 *        Recibe la posición del elemento en la lista.
 * @param hiloResaltado ID de hilo que debe mostrarse resaltado (o null para ninguno).
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class AdaptadorStock(
    private var items: MutableList<HiloStock> = mutableListOf(),
    private val onEliminarClick: (Int) -> Unit,
    private var hiloResaltado: String? = null /* para resaltar el hilo encontrado mediante búsqueda */
) : RecyclerView.Adapter<AdaptadorStock.StockViewHolder>() {

    /**
     * ViewHolder que contiene y gestiona los elementos visuales de cada fila.
     *
     * @property txtHilo TextView donde se muestra el ID del hilo.
     * @property txtMadejas TextView donde se muestra el número de madejas.
     * @property filaLayout Vista raíz de la fila, utilizada para cambiar el fondo.
     */
    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_hiloID)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_numeroMadejasTabla)
        val filaLayout: View = view /* contenedor completo de la fila (para cambiar fondo) */

        init {
            /* asigna el evento de pulsación larga a la fila completa */
            view.setOnLongClickListener {
                onEliminarClick(adapterPosition)
                true
            }
        }
    }

    /**
     * Infla el layout de cada fila y crea un [StockViewHolder].
     *
     * @param parent Grupo padre donde se añadirá la nueva vista.
     * @param viewType Tipo de vista (solo uno en este adaptador).
     * @return Un nuevo [StockViewHolder] con la vista inflada.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_tabla_filas_contenido, parent, false)
        return StockViewHolder(view)
    }

    /**
     * Vincula los datos de un [HiloStock] a los elementos de la fila.
     * - Muestra el ID y el número de madejas.
     * - Resalta la fila si su ID coincide con [hiloResaltado].
     *
     * @param holder ViewHolder que contiene las vistas a poblar.
     * @param position Posición del elemento en la lista [items].
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
     * Devuelve el número total de elementos que maneja el adaptador.
     *
     * @return Tamaño de la lista [items].
     */
    override fun getItemCount(): Int = items.size

    /**
     * Reemplaza la lista completa de hilos mostrados por [nuevaLista].
     * Notifica al adaptador para refrescar toda la vista.
     *
     * @param nuevaLista Nueva lista de [HiloStock] a mostrar.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloStock>) {
        items = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Ajusta qué hilo debe mostrarse resaltado.
     * Si [hiloId] es null, elimina cualquier resaltado previo.
     * Notifica al adaptador para refrescar las filas afectadas.
     *
     * @param hiloId ID de hilo a resaltar, o null para ninguno.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }
}
