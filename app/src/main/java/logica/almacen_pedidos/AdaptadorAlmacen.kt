package logica.almacen_pedidos

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador para el RecyclerView de pedidos almacenados.
 *
 * @param listaPedidos           Lista de PedidoGuardado a mostrar.
 * @param onDescargarClick       Lambda que se llama al pulsar el botón de descarga.
 * @param onPedidoRealizadoClick Lambda que se llama al pulsar el botón "realizado".
 */
class AdaptadorAlmacen(
    private var listaPedidos: List<PedidoGuardado>,
    private val onDescargarClick: (PedidoGuardado) -> Unit,
    private val onPedidoRealizadoClick: (PedidoGuardado) -> Unit
) : RecyclerView.Adapter<AdaptadorAlmacen.PedidoViewHolder>() {

    // nombre del pedido que debe resaltarse (o null para ningún resaltado)
    private var pedidoResaltado: String? = null

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
        holder.txtNombrePedido.text = pedido.nombre


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

        // 4) Pulsación larga para eliminar (esto ya lo tenías)
        holder.itemView.setOnLongClickListener {
            val contexto = holder.itemView.context as AlmacenPedidos
            contexto.dialogEliminarPedido(position)
            true
        }

        // 5) Resaltar si coincide con `pedidoResaltado`
        if (pedidoResaltado != null && pedidoResaltado.equals(pedido.nombre, ignoreCase = true)) {
            holder.itemView.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = listaPedidos.size

    /** Actualiza toda la lista de pedidos (sin alterar el resaltado). */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<PedidoGuardado>) {
        Log.d("AdaptadorAlmacen", "Actualizando lista. Tamaño nuevo: ${nuevaLista.size}")
        listaPedidos = nuevaLista
        notifyDataSetChanged()
    }

    /**
     * Fija el nombre de pedido que debe resaltarse y vuelve a dibujar.
     * Si `nombre` es null, quita el resaltado.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarPedido(nombre: String?) {
        pedidoResaltado = nombre
        notifyDataSetChanged()
    }
}
