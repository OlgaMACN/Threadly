import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import pedido_hilos.Grafico

class AdaptadorPedido(
    private val graficos: MutableList<Grafico>,
    private val onItemClick: (Int) -> Unit,   // ✅ este es el que faltaba
    private val onLongClick: (Int) -> Unit = {} // opcional por si lo usas
) : RecyclerView.Adapter<AdaptadorPedido.PedidoViewHolder>() {

    inner class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtVw_columnaNombreGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_columnaMadejasPedido)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position) // ✅ usa el callback que recibe la actividad
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
    }

    override fun getItemCount(): Int = graficos.size
}
