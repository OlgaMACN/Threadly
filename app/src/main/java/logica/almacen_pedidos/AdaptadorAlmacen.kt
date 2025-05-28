package logica.almacen_pedidos

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
 * Adaptador personalizado para el RecyclerView que muestra los pedidos almacenados en la pantalla AlmacenPedidos.
 *
 * Gestiona las acciones disponibles para cada pedido:
 * - Descargar el pedido como CSV.
 * - Marcar el pedido como realizado (actualiza stock y cambia el icono).
 * - Eliminar el pedido mediante pulsación larga.
 *
 * @param listaPedidos Lista de pedidos guardados que se mostrarán.
 * @param onDescargarClick Función lambda que se ejecuta al pulsar el botón de descarga.
 * @param onPedidoRealizadoClick Función lambda que se ejecuta al pulsar el botón de marcar como realizado.
 */
class AdaptadorAlmacen(
    private var listaPedidos: List<PedidoGuardado>,
    private val onDescargarClick: (PedidoGuardado) -> Unit,
    private val onPedidoRealizadoClick: (PedidoGuardado) -> Unit
) : RecyclerView.Adapter<AdaptadorAlmacen.PedidoViewHolder>() {

    /**
     * ViewHolder personalizado para representar cada fila de la tabla de pedidos.
     * Contiene el nombre del pedido y botones de acción.
     */
    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombrePedido: TextView = itemView.findViewById(R.id.txtVw_contenidoNombrePedido)
        val btnDescargar: ImageButton = itemView.findViewById(R.id.imgBtn_descargaPedido)
        val btnPedidoRealizado: ImageButton = itemView.findViewById(R.id.imgBtn_pedidoRealizado)
    }

    /**
     * Crea y retorna un nuevo ViewHolder inflando el layout de cada fila del RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        /* convierte (infla) el layout de una fila de pedido desde XML */
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.almacen_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(vista)
    }

    /**
     * Vincula los datos de un pedido con los elementos de la vista.
     *
     * @param holder ViewHolder que contiene las vistas a actualizar.
     * @param position Índice del pedido en la lista.
     */
    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        /* obtiene el pedido correspondiente a esta posición */
        val pedido = listaPedidos[position]

        /* asigna el nombre del pedido al TextView de la fila */
        holder.txtNombrePedido.text = pedido.nombre

        /* configura el botón de descarga con su evento */
        holder.btnDescargar.setOnClickListener {
            onDescargarClick(pedido) /* ejecuta la lambda definida al crear el adaptador */
        }

        /* si el pedido ya ha sido marcado como realizado */
        if (pedido.realizado) {
            /* desactiva el botón y cambia el icono y color a gris */
            holder.btnPedidoRealizado.isEnabled = false
            holder.btnPedidoRealizado.setImageResource(R.drawable.img_tick_pedido)
            holder.btnPedidoRealizado.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.grisPedidoRealizado),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            /* si el pedido no está realizado, muestra el icono activo */
            holder.btnPedidoRealizado.isEnabled = true
            holder.btnPedidoRealizado.setImageResource(R.drawable.img_tick_pedido)
            holder.btnPedidoRealizado.clearColorFilter()
        }

        /* al hacer clic en el botón de "pedido realizado" */
        holder.btnPedidoRealizado.setOnClickListener {
            if (holder.btnPedidoRealizado.isEnabled) {
                /* ejecuta la lambda que actualiza el estado del pedido y el stock */
                onPedidoRealizadoClick(pedido)
                /* notifica al adaptador para refrescar esta posición */
                notifyItemChanged(holder.adapterPosition)
            }
        }

        /* evento de pulsación larga para eliminar el pedido */
        holder.itemView.setOnLongClickListener {
            /* obtiene el contexto como actividad AlmacenPedidos y lanza el diálogo de eliminación */
            val contexto = holder.itemView.context as AlmacenPedidos
            contexto.dialogEliminarPedido(position)
            true /* retorna true para indicar que se ha consumido el evento */
        }
    }

    /**
     * Devuelve el número total de elementos en la lista.
     */
    override fun getItemCount(): Int = listaPedidos.size

    /**
     * Actualiza la lista de pedidos mostrada en el RecyclerView.
     *
     * @param nuevaLista Nueva lista de pedidos a mostrar.
     */
    fun actualizarLista(nuevaLista: List<PedidoGuardado>) {
        Log.d("Adaptador", "Actualizando lista. Tamaño nuevo: ${nuevaLista.size}")
        listaPedidos = nuevaLista
        notifyDataSetChanged() /* refresca el RecyclerView */
    }
}
