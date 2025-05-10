package pedido_hilos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorPedido(

    private val graficos: MutableList<Grafico>,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<AdaptadorPedido.PedidoViewHolder>() {

    inner class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /* cabecera de la tabla*/
        val txtNombre: TextView = view.findViewById(R.id.txtVw_columnaNombreGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_columnaMadejasPedido)

        init {
            view.setOnLongClickListener {
                onLongClick(adapterPosition)
                true
            }
        }
    }

    /* el adaptador no sabe qué hacer, y delega la acción que hace el usuario (mantener pulsado) a la actividad */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedido_tabla_filas_contenido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val grafico = graficos[position]
        holder.txtNombre.text = grafico.nombre
        holder.txtMadejas.text = grafico.countTela.toString()
    }

    override fun getItemCount(): Int = graficos.size
}
