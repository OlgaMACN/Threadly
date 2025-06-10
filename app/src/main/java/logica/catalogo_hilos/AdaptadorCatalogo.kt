package logica.catalogo_hilos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador para el RecyclerView que muestra el catálogo de hilos.
 *
 * Gestiona la conversión de objetos de dominio [HiloCatalogo] en filas visuales,
 * resalta búsquedas y tiene un callback para eliminación mediante long click.
 *
 * @property lista Lista mutable de hilos a mostrar.
 * @property onEliminarClick Callback a invocar cuando se solicita eliminar un hilo.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class AdaptadorCatalogo(
    private var lista: MutableList<HiloCatalogo>,
    private val onEliminarClick: (HiloCatalogo) -> Unit
) : RecyclerView.Adapter<AdaptadorCatalogo.ViewHolder>() {

    /**
     * Almacena el número de hilo que debe resaltarse tras una búsqueda
     */
    private var hiloResaltado: String? = null

    /**
     * ViewHolder que contiene las referencias a las vistas de cada fila.
     *
     * @property txtNum TextView que muestra el número del hilo.
     * @property txtNombre TextView que muestra el nombre del hilo.
     * @property viewColor View coloreada según el color del hilo.
     * @property txtNoColor TextView que indica si el color no es parseable.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNum: TextView = view.findViewById(R.id.txtVw_numHiloConsulta)
        val txtNombre: TextView = view.findViewById(R.id.txtVw_nombreHiloConsulta)
        val viewColor: View = view.findViewById(R.id.view_ColorHilo)
        val txtNoColor: TextView = view.findViewById(R.id.txtVw_colorImparseable)
    }

    /**
     * Infla el layout de fila y crea un [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.catalogo_tabla_filas_contenido, parent, false)
        return ViewHolder(v)
    }

    /**
     * Enlaza los datos de [HiloCatalogo] con las vistas de la fila en la posición dada.
     *
     * - Muestra número y nombre.
     * - Intenta parsear el color y aplicarlo; muestra mensaje si falla.
     * - Resalta la fila si coincide con [hiloResaltado].
     * - Configura long click para eliminar el hilo.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hilo = lista[position]
        holder.txtNum.text = hilo.numHilo
        holder.txtNombre.text = hilo.nombreHilo

        /* intentar parsear color y aplicarlo */
        try {
            val parsedColor = Color.parseColor(hilo.color)
            holder.viewColor.setBackgroundColor(parsedColor)
            holder.txtNoColor.visibility = View.GONE
        } catch (e: Exception) {
            /* si el formato de color no es válido, mostrar mensaje y fondo transparente */
            holder.viewColor.setBackgroundColor(Color.TRANSPARENT)
            holder.txtNoColor.visibility = View.VISIBLE
        }

        /* resaltar fila si se busca ese hilo */
        if (hilo.numHilo == hiloResaltado) {
            holder.itemView.setBackgroundResource(R.color.filaResaltadaBusqueda)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        /* long click invoca callback de eliminación */
        holder.itemView.setOnLongClickListener {
            onEliminarClick(hilo)
            true
        }
    }

    /**
     * Devuelve la cantidad de ítems en la lista.
     */
    override fun getItemCount(): Int = lista.size

    /**
     * Reemplaza la lista actual por [nuevaLista] y notifica el cambio.
     *
     * @param nuevaLista Lista de [HiloCatalogo] actualizada.
     */
    fun actualizarLista(nuevaLista: List<HiloCatalogo>) {
        lista = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Actualiza el número de hilo a resaltar en la siguiente renderización.
     *
     * @param hilo Número de hilo o null para eliminar resaltado.
     */
    fun resaltarHilo(hilo: String?) {
        hiloResaltado = hilo
    }
}
