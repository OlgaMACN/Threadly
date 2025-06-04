package logica.almacen_pedidos

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import modelo.PedidoGuardado
import com.threadly.R

/**
 * Adaptador para el RecyclerView de pedidos almacenados.
 *
 * @param listaPedidos               Lista de PedidoGuardado a mostrar.
 * @param onDescargarClick           Lambda que se llama al pulsar el botón de descarga.
 * @param onPedidoRealizadoClick     Lambda que se llama al pulsar el botón "realizado".
 * @param onNombrePedidoClick        Lambda que se llama al pulsar el TextView de nombre.
 */
class AdaptadorAlmacen(
    private var listaPedidos: List<PedidoGuardado>,
    private val onDescargarClick: (PedidoGuardado) -> Unit,
    private val onPedidoRealizadoClick: (PedidoGuardado) -> Unit,
    private val onNombrePedidoClick: (PedidoGuardado) -> Unit
) : RecyclerView.Adapter<AdaptadorAlmacen.PedidoViewHolder>() {

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombrePedido: TextView = itemView.findViewById(R.id.txtVw_contenidoNombrePedido)
        val btnDescargar: ImageButton = itemView.findViewById(R.id.imgBtn_descargaPedido)
        val btnPedidoRealizado: ImageButton = itemView.findViewById(R.id.imgBtn_pedidoRealizado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.almacen_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = listaPedidos[position]

        // 1) clic en el nombre abre el CSV
        holder.txtNombrePedido.text = pedido.nombre
        holder.txtNombrePedido.setOnClickListener {
            onNombrePedidoClick(pedido)
        }

        // 2) Botón de descarga
        holder.btnDescargar.setOnClickListener {
            onDescargarClick(pedido)
        }

        // 3) Icono "realizado"
        if (pedido.realizado) {
            holder.btnPedidoRealizado.isEnabled = false
            holder.btnPedidoRealizado.setImageResource(R.drawable.img_tick_pedido)
            holder.btnPedidoRealizado.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.grisPedidoRealizado),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            holder.btnPedidoRealizado.isEnabled = true
            holder.btnPedidoRealizado.setImageResource(R.drawable.img_tick_pedido)
            holder.btnPedidoRealizado.clearColorFilter()
        }
        holder.btnPedidoRealizado.setOnClickListener {
            if (holder.btnPedidoRealizado.isEnabled) {
                onPedidoRealizadoClick(pedido)
                notifyItemChanged(holder.adapterPosition)
            }
        }

        // 4) Pulsación larga para eliminar
        holder.itemView.setOnLongClickListener {
            val contexto = holder.itemView.context as AlmacenPedidos
            contexto.dialogEliminarPedido(position)
            true
        }
    }

    override fun getItemCount(): Int = listaPedidos.size

    fun actualizarLista(nuevaLista: List<PedidoGuardado>) {
        Log.d("AdaptadorAlmacen", "Actualizando lista. Tamaño nuevo: ${nuevaLista.size}")
        listaPedidos = nuevaLista
        notifyDataSetChanged()
    }
}
