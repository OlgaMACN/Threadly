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
 *
 * Cada fila muestra:
 *  - El código de hilo.
 *  - La cantidad de madejas actual.
 *  - Un EditText para modificar la cantidad (confirmando con "Done" o al perder foco).
 *
 * Gestiona:
 *  - Resaltado visual de búsqueda (color A) y de selección por clic (color B).
 *  - Persistencia de madejas en Room al confirmar la edición.
 *  - Cálculo y notificación del total de madejas tras cada modificación.
 *
 * @param hilos              Lista mutable de [HiloGrafico] que representa los hilos en el gráfico.
 * @param onClickHilo        Lambda que se invoca al hacer clic sobre el código de un hilo.
 *                           Recibe el [HiloGrafico] correspondiente.
 * @param onBorrarHilo    Lambda opcional que se invoca al mantener pulsada la fila de un hilo.
 *                           Recibe el [HiloGrafico] correspondiente.
 * @param onTotalChanged     Lambda que recibe el total de madejas (Int) tras cualquier cambio
 *                           confirmado de cantidad.
 * @param onUpdateMadejas    Lambda que se invoca para persistir en la base de datos
 *                           la cantidad confirmada de madejas de un [HiloGrafico].
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class AdaptadorGrafico(
    private val hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (HiloGrafico) -> Unit,
    private val onBorrarHilo: ((HiloGrafico) -> Unit)? = null,
    private val onTotalChanged: (Int) -> Unit,
    private val onUpdateMadejas: (HiloGrafico) -> Unit
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    /** ID de hilo resaltado por búsqueda (color A). */
    private var hiloResaltadoBusqueda: String? = null

    /** ID de hilo resaltado por clic (color B). */
    private var hiloResaltadoClick: String? = null

    /**
     * ViewHolder que contiene las vistas de cada fila de hilo:
     * - [txtHilo]: TextView con el código del hilo.
     * - [txtMadejas]: TextView con la cantidad actual de madejas.
     * - [edtModificar]: EditText para introducir la nueva cantidad.
     * - [filaLayout]: Vista raíz de la fila, usada para aplicar resaltados.
     */
    inner class HiloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_textoHiloGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasGrafico)
        val edtModificar: EditText = view.findViewById(R.id.txtVw_columnaModificarPedidoMadeja)
        val filaLayout: View = view
    }

    /**
     * Infla el layout de cada fila y crea un [HiloViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiloViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.pedidob_tabla_filas_contenido_grafico, parent, false)
        return HiloViewHolder(vista)
    }

    /**
     * Devuelve el número total de hilos gestionados.
     */
    override fun getItemCount(): Int = hilos.size

    /**
     * Asocia los datos de [HiloGrafico] a la vista de la fila:
     * 1. Muestra código y madejas.
     * 2. Inicializa el [EditText] con cualquier valor pendiente de edición.
     * 3. Configura el IME "Done" y la acción Enter para confirmar edición.
     * 4. Al perder foco, también confirma la edición.
     * 5. Aplica el fondo según resaltado de búsqueda o clic.
     * 6. Gestiona clic y pulsación larga.
     */
    override fun onBindViewHolder(holder: HiloViewHolder, position: Int) {
        val hiloItem = hilos[position]

        /* mostrar código y cantidad actual */
        holder.txtHilo.text = hiloItem.hilo
        holder.txtMadejas.text = hiloItem.madejas.toString()

        /* poner cantidad pendiente de edición si existe */
        holder.edtModificar.setText(hiloItem.cantidadModificar?.toString() ?: "")

        /* configurar acción "Hecho" en teclado para confirmar edición */
        holder.edtModificar.imeOptions = EditorInfo.IME_ACTION_DONE
        holder.edtModificar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER
                        && event.action == KeyEvent.ACTION_DOWN)
            ) {
                confirmarEdicion(holder, position)
                true
            } else {
                false
            }
        }

        /* confirmar edición también al perder foco */
        holder.edtModificar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                confirmarEdicion(holder, position)
            }
        }

        /* aplicar resaltado de búsqueda o clic */
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

        /* configurar clic para mostrar stock u otra acción */
        holder.txtHilo.setOnClickListener {
            onClickHilo(hiloItem)
        }
        /* configurar pulsación larga para eliminación */
        holder.itemView.setOnLongClickListener {
            onBorrarHilo?.invoke(hiloItem)
            true
        }
    }

    /**
     * Mecanismo común para confirmar la edición de madejas:
     * - Obtiene el valor introducido.
     * - Si es distinto del actual, actualiza la lista, notifica el cambio,
     *   recalcula total y persiste en base de datos.
     */
    private fun confirmarEdicion(holder: HiloViewHolder, position: Int) {
        val hiloItem = hilos[position]
        val texto = holder.edtModificar.text.toString().trim()
        val nuevaCant = texto.toIntOrNull() ?: hiloItem.madejas
        if (nuevaCant != hiloItem.madejas) {
            hilos[position] = hiloItem.copy(madejas = nuevaCant, cantidadModificar = null)
            notifyItemChanged(position)
            onTotalChanged(calcularTotal())
            onUpdateMadejas(hilos[position])
        }
        holder.edtModificar.clearFocus()
    }

    /**
     * Reemplaza la lista interna por [nueva], notifica al adaptador
     * y recalcula el total de madejas.
     *
     * @param nueva Nueva colección de [HiloGrafico].
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nueva: List<HiloGrafico>) {
        hilos.clear()
        hilos.addAll(nueva)
        notifyDataSetChanged()
        onTotalChanged(calcularTotal())
    }

    /**
     * Resalta el hilo con ID [hiloId] como resultado de búsqueda (color A).
     * Quita cualquier resaltado de clic.
     *
     * @param hiloId ID de hilo a resaltar, o null para quitar resaltados.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHiloBusqueda(hiloId: String?) {
        hiloResaltadoBusqueda = hiloId
        hiloResaltadoClick = null
        notifyDataSetChanged()
    }

    /**
     * Resalta el hilo con ID [hiloId] por selección de clic (color B).
     * Si ya estaba resaltado, lo quita.
     * Quita cualquier resaltado de búsqueda.
     *
     * @param hiloId ID de hilo a alternar resaltado, o null para quitar resaltados.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHiloClick(hiloId: String?) {
        hiloResaltadoBusqueda = null
        hiloResaltadoClick = if (hiloResaltadoClick == hiloId) null else hiloId
        notifyDataSetChanged()
    }

    /**
     * Devuelve la lista interna de hilos del gráfico.
     *
     * @return MutableList de [HiloGrafico].
     */
    fun obtenerLista(): MutableList<HiloGrafico> = hilos

    /**
     * Devuelve el hilo resaltado por clic, si existe.
     *
     * @return ID de hilo resaltado por clic, o null.
     */
    fun obtenerHiloResaltadoClick(): String? = hiloResaltadoClick

    /**
     * Suma todas las madejas de la lista y devuelve el total.
     *
     * @return Total de madejas.
     */
    private fun calcularTotal(): Int = hilos.sumOf { it.madejas }
}
