package logica.catalogo_hilos

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador para el RecyclerView que muestra la tabla del catálogo de hilos en la app Threadly.
 * Presenta cada hilo con su número, nombre y color correspondiente, permite detectar pulsaciones largas
 * y resaltar un hilo específico.
 *
 * @param items Lista mutable de objetos [HiloCatalogo] que representa los hilos a mostrar.
 * @param onEliminarClick Función lambda que se ejecuta al mantener pulsado un ítem (ej. para editar o eliminar); recibe la posición del ítem.
 * @param hiloResaltado Número del hilo a resaltar (por ejemplo, si se ha buscado mediante el buscador).
 */
class AdaptadorCatalogo(
    private var items: MutableList<HiloCatalogo> = mutableListOf(),
    private val onEliminarClick: (Int) -> Unit,
    private var hiloResaltado: String? = null
) : RecyclerView.Adapter<AdaptadorCatalogo.CatalogoViewHolder>() {

    /**
     * ViewHolder personalizado que representa una fila en la tabla del catálogo.
     * Contiene referencias a las vistas necesarias para mostrar el contenido de un hilo.
     *
     * @param view Vista inflada desde el layout XML que representa una fila.
     */
    inner class CatalogoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_numHiloConsulta)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_nombreHiloConsulta)
        val viewColorSwatch: View = view.findViewById(R.id.view_ColorHilo)
        val txtColorError: TextView = view.findViewById(R.id.txtVw_colorImparseable)
        val filaLayout: View =
            view /* vista completa de la fila (para cambiar el fondo al resaltar) */

        init {
            /* fetecta pulsación larga en una fila y lanza la acción definida */
            view.setOnLongClickListener {
                onEliminarClick(adapterPosition)
                true
            }
        }
    }

    /**
     * Crea una nueva instancia del ViewHolder inflando el layout correspondiente a la fila de la tabla.
     *
     * @param parent Vista padre del RecyclerView.
     * @param viewType Tipo de vista (no se usa aquí, ya que solo hay un tipo).
     * @return Un nuevo [CatalogoViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.catalogo_tabla_filas_contenido, parent, false)
        return CatalogoViewHolder(view)
    }

    /**
     * Asocia los datos de un hilo con el ViewHolder correspondiente en la posición dada.
     * También aplica el color y, si corresponde, el resaltado de la fila.
     *
     * @param holder ViewHolder que se está actualizando.
     * @param position Posición del ítem en la lista.
     */
    override fun onBindViewHolder(holder: CatalogoViewHolder, position: Int) {
        val item = items[position]
        holder.txtHilo.text = item.numHilo.toString()
        holder.txtMadejas.text = item.nombreHilo

        val colorStr = item.color
        if (!colorStr.isNullOrBlank()) {
            try {
                /* se asegura de que el color tenga el prefijo "#" y se convierte a formato Color */
                val parsed =
                    Color.parseColor(if (colorStr.startsWith("#")) colorStr else "#$colorStr")
                holder.viewColorSwatch.setBackgroundColor(parsed)
                holder.viewColorSwatch.visibility = View.VISIBLE
                holder.txtColorError.visibility = View.GONE
            } catch (e: IllegalArgumentException) {
                /* si el color no es válido, se oculta la vista de color y se muestra el error */
                holder.viewColorSwatch.visibility = View.GONE
                holder.txtColorError.visibility = View.VISIBLE
            }
        } else {
            /* si el color es nulo o vacío, se muestra el error */
            holder.viewColorSwatch.visibility = View.GONE
            holder.txtColorError.visibility = View.VISIBLE
        }

        /* aplica un fondo de resaltado si el hilo coincide con el buscado */
        if (item.numHilo.toString() == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }
    }

    /**
     * Devuelve el número total de elementos en la lista del catálogo.
     *
     * @return Número de hilos mostrados.
     */
    override fun getItemCount(): Int = items.size

    /**
     * Actualiza por completo la lista de hilos del catálogo con una nueva lista.
     * Este método fuerza la recarga completa del RecyclerView.
     *
     * @param nuevaLista Lista nueva de hilos [HiloCatalogo] que reemplazará a la actual.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloCatalogo>) {
        items = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Resalta visualmente un hilo de la tabla, comparando su número con [numHilo].
     * Si el número coincide con el valor indicado, se aplica un fondo especial.
     *
     * @param numHilo Número de hilo a resaltar, o null si se desea limpiar el resaltado.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(numHilo: String?) {
        hiloResaltado = numHilo
        notifyDataSetChanged()
    }
}
