package pedido_hilos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorPedido(
    private var graficos: MutableList<Grafico>,
    private val onItemClick: (Grafico) -> Unit,
    private val onLongClick: (Int) -> Unit = {},
) : RecyclerView.Adapter<AdaptadorPedido.PedidoViewHolder>() {

    private var graficoResaltado: String? = null

    inner class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtVw_textoNombreGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasPedido)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(graficos[position])
                }
            }

            view.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLongClick(position)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedido_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val grafico = graficos[position]
        holder.txtNombre.text = grafico.nombre
        holder.txtMadejas.text = grafico.madejas.toString()

        if (grafico.nombre.equals(graficoResaltado, ignoreCase = true)) {
            holder.itemView.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun getItemCount(): Int = graficos.size

    /* actualizar la tabla tras borrar, eliminar o resaltar */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<Grafico>) {
        graficos = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    /* resaltar un elemento de la tabla cuando se ha buscado */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarGrafico(nombre: String?) {
        graficoResaltado = nombre
        notifyDataSetChanged()
    }

    /* suma total de las madejas */
    fun obtenerTotalMadejas(): Int {
        return graficos.sumOf { it.madejas }
    }

}
