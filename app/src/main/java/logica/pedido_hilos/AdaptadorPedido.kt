package logica.pedido_hilos

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador personalizado para el RecyclerView que muestra los gráficos añadidos
 * a un pedido en la pantalla de edición de pedidos.
 *
 * Cada fila muestra el nombre del gráfico y permite:
 *  - Clic corto para editar el gráfico (invoca [onEditarGrafico]).
 *  - Clic largo para eliminar el gráfico (invoca [onEliminarGrafico]).
 *  - Resaltar visualmente un gráfico buscado.
 *
 * @property graficos Lista mutable de objetos [Grafico] representando el pedido actual.
 * @property onEditarGrafico Lambda que se ejecuta al pulsar un gráfico; recibe el [Grafico].
 * @property onEliminarGrafico Lambda que se ejecuta al mantener pulsado un gráfico;
 *        recibe la posición en la lista.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class AdaptadorPedido(
    private var graficos: MutableList<Grafico>,
    private val onEditarGrafico: (Grafico) -> Unit,
    private val onEliminarGrafico: (Int) -> Unit = {}
) : RecyclerView.Adapter<AdaptadorPedido.PedidoViewHolder>() {

    /**
     * Nombre del gráfico que debe mostrarse resaltado en la lista,
     * normalmente tras una operación de búsqueda. Null si no hay resaltado.
     */
    private var graficoResaltado: String? = null

    /**
     * ViewHolder que contiene y gestiona las vistas de una fila individual
     * en el RecyclerView de pedidos.
     *
     * @param view Vista inflada correspondiente al layout de fila.
     */
    inner class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /** TextView donde se muestra el nombre del gráfico. */
        val txtNombre: TextView = view.findViewById(R.id.txtVw_textoNombreGrafico)

        init {
            /* configurar el clic corto para edición */
            view.setOnClickListener {
                Log.d(
                    "AdaptadorPedido",
                    "Fila clicada en posición $adapterPosition"
                ) /* log depuración */
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditarGrafico(graficos[position])
                }
            }
            /* configurar el clic largo para eliminación */
            view.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEliminarGrafico(position)
                }
                true
            }
        }
    }

    /**
     * Infla la vista de cada ítem y crea un [PedidoViewHolder].
     *
     * @param parent ViewGroup padre donde se añadirá la nueva vista.
     * @param viewType Tipo de vista (no se utiliza en este adaptador).
     * @return Nueva instancia de [PedidoViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedido_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(view)
    }

    /**
     * Vincula los datos de un [Grafico] a los elementos de la vista.
     * - Muestra el nombre en [txtNombre].
     * - Resalta el fondo si coincide con [graficoResaltado].
     *
     * @param holder ViewHolder con las vistas a poblar.
     * @param position Posición del elemento dentro de [graficos].
     */
    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val grafico = graficos[position]
        holder.txtNombre.text = grafico.nombre

        if (grafico.nombre.equals(graficoResaltado, ignoreCase = true)) {
            holder.itemView.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }
    }

    /**
     * Devuelve el número total de gráficos actualmente en la lista.
     *
     * @return Tamaño de la lista [graficos].
     */
    override fun getItemCount(): Int = graficos.size

    /**
     * Reemplaza la lista completa de gráficos por [nuevaLista] y notifica al adaptador
     * para refrescar todas las filas.
     *
     * @param nuevaLista Nueva colección de [Grafico] a mostrar.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<Grafico>) {
        graficos = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Establece qué gráfico debe mostrarse resaltado visualmente.
     * Llama a `notifyDataSetChanged()` para actualizar el RecyclerView.
     *
     * @param nombre Nombre del gráfico a resaltar, o null para quitar resaltado.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarGrafico(nombre: String?) {
        graficoResaltado = nombre
        notifyDataSetChanged()
    }

}
