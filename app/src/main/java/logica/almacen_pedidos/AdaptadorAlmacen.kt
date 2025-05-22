package logica.almacen_pedidos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorAlmacen(
    private var listaPedidos: List<PedidoGuardado>,
    private val onDescargarClick: (PedidoGuardado) -> Unit
) : RecyclerView.Adapter<AdaptadorAlmacen.PedidoViewHolder>() {

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombrePedido: TextView = itemView.findViewById(R.id.txtVw_contenidoNombrePedido)
        val btnDescargar: ImageButton = itemView.findViewById(R.id.imgBtn_descargaPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.almacen_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = listaPedidos[position]
        holder.txtNombrePedido.text = pedido.nombre

        holder.btnDescargar.setOnClickListener {
            onDescargarClick(pedido)
        }

        holder.itemView.setOnLongClickListener {
            val contexto = holder.itemView.context as AlmacenPedidos
            contexto.dialogEliminarPedido(holder.adapterPosition)
            true
        }
    }

    override fun getItemCount(): Int = listaPedidos.size

    fun actualizarLista(nuevaLista: List<PedidoGuardado>) {
        listaPedidos = nuevaLista
        notifyDataSetChanged()
    }
}
