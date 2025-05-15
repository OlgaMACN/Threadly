package grafico_pedido

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorGrafico(
    private var hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (String) -> Unit,
    private var hiloResaltado: String? = null
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    inner class HiloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_columnaHiloGrafico)
        val txtPuntadas: TextView = view.findViewById(R.id.txtVw_columnaPuntadasGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_columnaMadejasGrafico)
        val filaLayout: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiloViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedidob_tabla_filas_contenido_grafico, parent, false)
        return HiloViewHolder(vista)
    }

    override fun getItemCount(): Int = hilos.size

    override fun onBindViewHolder(holder: HiloViewHolder, position: Int) {
        val hilo = hilos[position]
        holder.txtHilo.text = hilo.hilo
        holder.txtPuntadas.text = hilo.puntadas.toString()
        holder.txtMadejas.text = hilo.madejas.toString()

        if (hilo.hilo == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }

        holder.txtHilo.setOnClickListener {
            onClickHilo(hilo.hilo)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloGrafico>) {
        hilos = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }
}
