package logica.catalogo_hilos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorCatalogo(
    private var lista: MutableList<HiloCatalogo>,
    private val onEliminarClick: (HiloCatalogo) -> Unit
) : RecyclerView.Adapter<AdaptadorCatalogo.ViewHolder>() {

    private var hiloResaltado: String? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNum = view.findViewById<TextView>(R.id.txtVw_numHiloConsulta)
        val txtNombre = view.findViewById<TextView>(R.id.txtVw_nombreHiloConsulta)
        val viewColor = view.findViewById<View>(R.id.view_ColorHilo)
        val txtNoColor = view.findViewById<TextView>(R.id.txtVw_colorImparseable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.catalogo_tabla_filas_contenido, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hilo = lista[position]
        holder.txtNum.text = hilo.numHilo
        holder.txtNombre.text = hilo.nombreHilo

        try {
            val parsedColor = Color.parseColor(hilo.color)
            holder.viewColor.setBackgroundColor(parsedColor)
            holder.txtNoColor.visibility = View.GONE
        } catch (e: Exception) {
            holder.viewColor.setBackgroundColor(Color.TRANSPARENT)
            holder.txtNoColor.visibility = View.VISIBLE
        }

        // resaltar si corresponde
        if (hilo.numHilo == hiloResaltado) {
            holder.itemView.setBackgroundResource(R.color.filaResaltadaBusqueda)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Aquí puedes usar click largo o un botón si luego añades uno
        holder.itemView.setOnLongClickListener {
            onEliminarClick(hilo)
            true
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<HiloCatalogo>) {
        lista = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    fun resaltarHilo(hilo: String?) {
        hiloResaltado = hilo
    }
}
