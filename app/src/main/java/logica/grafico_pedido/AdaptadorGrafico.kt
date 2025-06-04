package logica.grafico_pedido

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

/**
 * Adaptador para mostrar y gestionar la lista de hilos de un gráfico en un RecyclerView.
 * Ahora la cantidad de madejas solo se persiste al confirmar (“Done” en el teclado) o al perder foco,
 * en lugar de actualizarse en tiempo real.
 *
 * @param hilos             Lista mutable de [HiloGrafico]
 * @param onClickHilo       Lambda que se invoca al hacer clic en el TextView de cada hilo.
 * @param onLongClickHilo   Lambda opcional para pulsación larga (ej. eliminar).
 * @param onTotalChanged    Lambda que recibe el total de madejas (Int), se llama cada vez que cambian las cantidades confirmadas.
 * @param onUpdateMadejas   Lambda que se invoca para persistir la cantidad confirmada de madejas en Room.
 */
class AdaptadorGrafico(
    private val hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (HiloGrafico) -> Unit,
    private val onLongClickHilo: ((HiloGrafico) -> Unit)? = null,
    private val onTotalChanged: (Int) -> Unit,
    private val onUpdateMadejas: (HiloGrafico) -> Unit
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    // Para resaltar búsquedas (color A) o clics (color B)
    private var hiloResaltadoBusqueda: String? = null
    private var hiloResaltadoClick: String? = null

    inner class HiloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_textoHiloGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasGrafico)
        val edtModificar: EditText = view.findViewById(R.id.txtVw_columnaModificarPedidoMadeja)
        val filaLayout: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiloViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedidob_tabla_filas_contenido_grafico, parent, false)
        return HiloViewHolder(vista)
    }

    override fun getItemCount(): Int = hilos.size

    override fun onBindViewHolder(holder: HiloViewHolder, position: Int) {
        val hiloItem = hilos[position]

        // 1) Mostrar código y cantidad actual
        holder.txtHilo.text = hiloItem.hilo
        holder.txtMadejas.text = hiloItem.madejas.toString()

        // 2) Inicializar el EditText con la cantidad modificada previa (si hubo)
        holder.edtModificar.setText(hiloItem.cantidadModificar?.toString() ?: "")

        // 3) Configurar “Done” en teclado para guardar la cantidad confirmada
        holder.edtModificar.imeOptions = EditorInfo.IME_ACTION_DONE
        holder.edtModificar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val texto = holder.edtModificar.text.toString().trim()
                val nuevaCant = texto.toIntOrNull() ?: hiloItem.madejas

                // Solo si ha cambiado la cantidad real
                if (nuevaCant != hiloItem.madejas) {
                    // 3a) Actualizar objeto en memoria
                    hilos[position] = hiloItem.copy(madejas = nuevaCant, cantidadModificar = null)
                    // 3b) Notificar que esta fila cambió
                    notifyItemChanged(position)
                    // 3c) Recalcular total de madejas
                    onTotalChanged(calcularTotal())
                    // 3d) Persistir en BD
                    onUpdateMadejas(hilos[position])
                }

                // Limpiar foco para ocultar teclado
                holder.edtModificar.clearFocus()
                true
            } else {
                false
            }
        }

        // 4) Al perder foco, también confirmamos la edición (en caso de no pulsar “Done”)
        holder.edtModificar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val texto = holder.edtModificar.text.toString().trim()
                val nuevaCant = texto.toIntOrNull() ?: hiloItem.madejas
                if (nuevaCant != hiloItem.madejas) {
                    hilos[position] = hiloItem.copy(madejas = nuevaCant, cantidadModificar = null)
                    notifyItemChanged(position)
                    onTotalChanged(calcularTotal())
                    onUpdateMadejas(hilos[position])
                }
            }
        }

        // 5) Decidir fondo según resaltado de búsqueda o clic
        when {
            hiloItem.hilo == hiloResaltadoBusqueda -> {
                holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
            }
            hiloItem.hilo == hiloResaltadoClick -> {
                holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_hilografico_stock)
            }
            else -> {
                holder.filaLayout.setBackgroundResource(android.R.color.transparent)
            }
        }

        // 6) Click en el TextView del hilo → mostrar stock u otra acción
        holder.txtHilo.setOnClickListener {
            onClickHilo(hiloItem)
        }
        // 7) Long click en la fila → eliminar
        holder.itemView.setOnLongClickListener {
            onLongClickHilo?.invoke(hiloItem)
            true
        }
    }

    /**
     * Actualiza la lista completa de hilos, notifica cambios y recalcula total.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nueva: List<HiloGrafico>) {
        hilos.clear()
        hilos.addAll(nueva)
        notifyDataSetChanged()
        onTotalChanged(calcularTotal())
    }

    /**
     * Resalta (color A) el [hiloId] como resultado de búsqueda.
     * Limpia resaltado por clic.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHiloBusqueda(hiloId: String?) {
        hiloResaltadoBusqueda = hiloId
        hiloResaltadoClick = null
        notifyDataSetChanged()
    }

    /**
     * Resalta (color B) el [hiloId] porque se hizo clic para ver stock.
     * Limpia resaltado de búsqueda.
     */
    @SuppressLint("NotifyDataetChanged")
    fun resaltarHiloClick(hiloId: String?) {
        hiloResaltadoBusqueda = null
        hiloResaltadoClick = if (hiloResaltadoClick == hiloId) null else hiloId
        notifyDataSetChanged()
    }

    /**
     * Devuelve la lista interna de HiloGrafico.
     */
    fun obtenerLista(): MutableList<HiloGrafico> = hilos

    /**
     * Suma todas las madejas para devolver el total.
     */
    private fun calcularTotal(): Int {
        return hilos.sumOf { it.madejas }
    }
    /**
     * Permite obtener cuál es el hilo resaltado por clic (para mostrar/ocultar stock).
     */
    fun obtenerHiloResaltadoClick(): String? = hiloResaltadoClick
}
