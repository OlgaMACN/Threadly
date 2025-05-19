package logica.grafico_pedido

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class AdaptadorGrafico(
    private val hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (HiloGrafico) -> Unit,
    private val onLongClickHilo: ((HiloGrafico) -> Unit)? = null,
    private var hiloResaltado: String? = null,
    private val onTotalChanged: ((Int) -> Unit) /* para coger las madejas modificadas */
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    inner class HiloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_textoHiloGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasGrafico)
        val edtModificar: EditText = view.findViewById(R.id.txtVw_columnaModificarPedidoMadeja)
        val filaLayout: View = view

        /* para poder guardar el valor actual y no acumular listeners */
        var textWatcher: TextWatcher? = null
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
        holder.txtMadejas.text = hilo.madejas.toString()

        /* se elimina el textWatcher anterior para evitar duplicados o cosas extrañas */
        holder.textWatcher?.let {
            holder.edtModificar.removeTextChangedListener(it)
        }

        /* y se carga el valor predeterminado o editado */
        holder.edtModificar.setText(hilo.cantidadModificar?.toString() ?: "")

        /* se crea un textWatcher nuevo para la fila que se ha editado */
        holder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                val cantidad = when (texto) {
                    "-" -> 0
                    "" -> null
                    else -> texto.toIntOrNull()
                }
                hilo.cantidadModificar = cantidad
                onTotalChanged(calcularTotal())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        /* y se añade el nuevo textWatcher a la lista */
        holder.edtModificar.addTextChangedListener(holder.textWatcher)

        if (hilo.hilo == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }

        holder.txtHilo.setOnClickListener {
            onClickHilo(hilo)
        }
        holder.itemView.setOnLongClickListener {
            onLongClickHilo?.invoke(hilos[position])
            true
        }
        onTotalChanged(calcularTotal()) /* cada vez que se pinta la lista, se actualiza el cálculo */
    }

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloGrafico>) {
        hilos.clear()
        hilos.addAll(nuevaLista)
        notifyDataSetChanged()
        onTotalChanged(calcularTotal())
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }

    private fun calcularTotal(): Int {
        return hilos.sumOf { it.cantidadModificar ?: it.madejas }
    }

}
