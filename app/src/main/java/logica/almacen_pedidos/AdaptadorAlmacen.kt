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
 * Adaptador para el RecyclerView que muestra los pedidos almacenados.
 *
 * Cada fila incluye:
 *  - El nombre del pedido.
 *  - Botón para descargar el pedido en CSV.
 *  - Botón para marcar el pedido como realizado.
 *  - Pulsación larga para eliminar el pedido.
 *  - Resaltado visual tras búsquedas.
 *
 * @property listaPedidos           Lista de [PedidoGuardado] a mostrar.
 * @property onDescargarClick       Lambda que se invoca al pulsar el botón de descarga.
 * @property onPedidoRealizadoClick Lambda que se invoca al pulsar el botón "realizado".
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @param listaPedidos Lista de pedidos guardados que se mostrarán.
 * @param onDescargarClick Función lambda que se ejecuta al pulsar el botón de descarga.
 * @param onPedidoRealizadoClick Función lambda que se ejecuta al pulsar el botón de marcar como realizado.
 *
 *
 */
class AdaptadorAlmacen(
    private var listaPedidos: List<PedidoGuardado>,
    private val onDescargarClick: (PedidoGuardado) -> Unit,
    private val onPedidoRealizadoClick: (PedidoGuardado) -> Unit
) : RecyclerView.Adapter<AdaptadorAlmacen.PedidoViewHolder>() {

    /** Nombre de pedido que debe mostrarse resaltado (o null para ninguno). */
    private var pedidoResaltado: String? = null

    /**
     * ViewHolder que encapsula las vistas de una fila:
     *  - [txtNombrePedido]: TextView con el nombre del pedido.
     *  - [btnDescargar]: ImageButton para descargar el pedido.
     *  - [btnPedidoRealizado]: ImageButton para marcar como realizado.
     */
    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombrePedido: TextView   = itemView.findViewById(R.id.txtVw_contenidoNombrePedido)
        val btnDescargar: ImageButton   = itemView.findViewById(R.id.imgBtn_descargaPedido)
        val btnPedidoRealizado: ImageButton = itemView.findViewById(R.id.imgBtn_pedidoRealizado)
    }

    /**
     * Infla el layout de la fila y crea un [PedidoViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.almacen_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(vista)
    }

    /**
     * Vincula los datos de un [PedidoGuardado] a las vistas de la fila:
     * - Muestra el nombre del pedido.
     * - Configura el botón de descarga.
     * - Ajusta el estado e icono del botón "realizado".
     * - Asigna pulsación larga para eliminar mediante [AlmacenPedidos.dialogEliminarPedido].
     * - Aplica resaltado si coincide con [pedidoResaltado].
     *
     * @param holder   ViewHolder que contiene las vistas a poblar.
     * @param position Posición del pedido en [listaPedidos].
     */
    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = listaPedidos[position]
        holder.txtNombrePedido.text = pedido.nombre

        holder.btnDescargar.setOnClickListener {
            onDescargarClick(pedido)
        }

        /* icono y estado "realizado" */
        if (pedido.realizado) {
            holder.btnPedidoRealizado.isEnabled = false
            holder.btnPedidoRealizado.setImageResource(R.drawable.img_tick_pedido)
            holder.btnPedidoRealizado.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.grisDesactivado),
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

        /* pulsación larga para eliminar */
        holder.itemView.setOnLongClickListener {
            val contexto = holder.itemView.context as AlmacenPedidos
            contexto.dialogEliminarPedido(position)
            true
        }

        /* resaltar si coincide con `pedidoResaltado` */
        if (pedidoResaltado != null && pedidoResaltado.equals(pedido.nombre, ignoreCase = true)) {
            holder.itemView.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Devuelve el número de pedidos en la lista.
     */
    override fun getItemCount(): Int = listaPedidos.size

    /**
     * Reemplaza la lista completa de pedidos por [nuevaLista] y notifica al adaptador.
     * Mantiene el estado de resaltado hasta que se llame a [resaltarPedido].
     *
     * @param nuevaLista Nueva lista de [PedidoGuardado] a mostrar.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<PedidoGuardado>) {
        Log.d("AdaptadorAlmacen", "Actualizando lista. Tamaño nuevo: ${nuevaLista.size}")
        listaPedidos = nuevaLista
        notifyDataSetChanged()
    }

    /**
     * Fija qué nombre de pedido debe resaltarse visualmente.
     * Si [nombre] es null, quita cualquier resaltado previo.
     *
     * @param nombre Nombre de pedido a resaltar, o null para ninguno.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarPedido(nombre: String?) {
        pedidoResaltado = nombre
        notifyDataSetChanged()
    }
}
