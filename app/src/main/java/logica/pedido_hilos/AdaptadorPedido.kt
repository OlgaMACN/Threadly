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
 * AdaptadorPedido es el adaptador personalizado para el RecyclerView que muestra
 * los gráficos añadidos a un pedido en la pantalla de edición de pedidos.
 *
 * Este adaptador permite visualizar el nombre del gráfico y la cantidad total
 * de madejas necesarias, así como gestionar clics y pulsaciones largas sobre los ítems.
 *
 * @property graficos Lista mutable de objetos [Grafico] que representan los gráficos en el pedido.
 * @property onItemClick Lambda que se ejecuta al pulsar un gráfico (para editarlo).
 * @property onEliminarGrafico Lambda que se ejecuta al mantener pulsado un gráfico (para eliminarlo).
 * * @author Olga y Sandra Macías Aragón
 *
 */
class AdaptadorPedido(
    private var graficos: MutableList<Grafico>,
    private val onItemClick: (Grafico) -> Unit,
    private val onEliminarGrafico: (Int) -> Unit = {},
) : RecyclerView.Adapter<AdaptadorPedido.PedidoViewHolder>() {

    /* nombre del gráfico resaltado tras una búsqueda, para destacar visualmente en la lista. */
    private var graficoResaltado: String? = null

    /**
     * ViewHolder que contiene las referencias a las vistas de cada fila del RecyclerView.
     *
     * @param view Vista inflada del layout de fila individual.
     */
    inner class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtVw_textoNombreGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasPedido)

        init {
            /* manejo del clic corto para editar el gráfico */
            view.setOnClickListener {
                Log.d("AdaptadorPedido", "Fila clicada en posición $adapterPosition")
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(graficos[position])
                }
            }

            /* manejo del clic largo para eliminar el gráfico */
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
     * Infla la vista de cada ítem del RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedido_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(view)
    }

    /**
     * Asocia los datos del gráfico a los elementos de la vista.
     */
    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val grafico = graficos[position]
        holder.txtNombre.text = grafico.nombre
        holder.txtMadejas.text = grafico.madejas.toString()

        /* resaltar si es el gráfico buscado */
        if (grafico.nombre.equals(graficoResaltado, ignoreCase = true)) {
            holder.itemView.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }
    }

    /**
     * Devuelve la cantidad total de ítems en la lista.
     */
    override fun getItemCount(): Int = graficos.size

    /**
     * Actualiza la lista de gráficos del pedido con una nueva.
     *
     * @param nuevaLista Nueva lista de objetos [Grafico] a mostrar.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<Grafico>) {
        graficos = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Resalta visualmente el gráfico cuyo nombre coincide con el proporcionado.
     *
     * @param nombre Nombre del gráfico a resaltar.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarGrafico(nombre: String?) {
        graficoResaltado = nombre
        notifyDataSetChanged()
    }

    /**
     * Calcula el total de madejas requeridas para todos los gráficos del pedido.
     *
     * @return Número total de madejas.
     */
    fun obtenerTotalMadejas(): Int {
        return graficos.sumOf { it.madejas }
    }
}
