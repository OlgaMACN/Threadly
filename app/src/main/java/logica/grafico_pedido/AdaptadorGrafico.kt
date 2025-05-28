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

/**
 * Adaptador para mostrar y gestionar la lista de hilos de un gráfico en un RecyclerView.
 * Permite modificar la cantidad de madejas para cada hilo, resaltar un hilo concreto,
 * y notificar cambios totales de madejas.
 *
 * @property hilos Lista mutable de objetos [HiloGrafico] que representan los hilos del gráfico.
 * @property onClickHilo Función lambda que se ejecuta al pulsar un hilo (parámetro: hilo pulsado).
 * @property onLongClickHilo Función lambda opcional para pulsación larga sobre un hilo (para borrar, por ejemplo).
 * @property hiloResaltado Identificador del hilo que debe mostrarse resaltado (por defecto, ninguno).
 * @property onTotalChanged Función lambda que recibe la suma total actualizada de madejas, llamada tras cada cambio.
 *
 *
 * * @author Olga y Sandra Macías Aragón
 */
class AdaptadorGrafico(
    private val hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (HiloGrafico) -> Unit,
    private val onLongClickHilo: ((HiloGrafico) -> Unit)? = null,
    private var hiloResaltado: String? = null,
    private val onTotalChanged: ((Int) -> Unit)
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    /**
     * ViewHolder que contiene los elementos visuales de cada fila para un hilo.
     * Contiene un TextView para el nombre del hilo, otro para madejas y un EditText
     * para modificar la cantidad de madejas pedidas.
     *
     * @property txtHilo TextView que muestra el código o nombre del hilo.
     * @property txtMadejas TextView que muestra la cantidad total de madejas calculadas.
     * @property edtModificar EditText para modificar la cantidad de madejas pedidas.
     * @property filaLayout Vista raíz de la fila para cambiar el fondo (resaltado).
     * @property textWatcher Listener que controla los cambios del EditText para evitar duplicados.
     */
    inner class HiloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHilo: TextView = view.findViewById(R.id.txtVw_textoHiloGrafico)
        val txtMadejas: TextView = view.findViewById(R.id.txtVw_textoMadejasGrafico)
        val edtModificar: EditText = view.findViewById(R.id.txtVw_columnaModificarPedidoMadeja)
        val filaLayout: View = view
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

        /* mostrar código y cantidad de madejas */
        holder.txtHilo.text = hilo.hilo
        holder.txtMadejas.text = hilo.madejas.toString()

        /* eliminar TextWatcher previo para evitar llamadas múltiples */
        holder.textWatcher?.let {
            holder.edtModificar.removeTextChangedListener(it)
        }

        /* cargar el valor editable: si se ha modificado, mostrar la cantidad modificada, sino vacío */
        holder.edtModificar.setText(hilo.cantidadModificar?.toString() ?: "")

        /* crear nuevo TextWatcher para actualizar la cantidad modificada */
        holder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                val cantidad = when (texto) {
                    "-" -> 0
                    "" -> null
                    else -> texto.toIntOrNull()
                }
                hilo.cantidadModificar = cantidad

                /* notificar el cambio total tras editar una cantidad */
                onTotalChanged(calcularTotal())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        /* añadir el nuevo TextWatcher al EditText */
        holder.edtModificar.addTextChangedListener(holder.textWatcher)

        /* resaltar el hilo si coincide con el solicitado */
        if (hilo.hilo == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }
        /* click simple llama a la función proporcionada */
        holder.txtHilo.setOnClickListener {
            onClickHilo(hilo)
        }
        /* click largo llama a la función de borrar (si existe) */
        holder.itemView.setOnLongClickListener {
            onLongClickHilo?.invoke(hilos[position])
            true
        }
        /* actualizar total al pintar cada fila (asegura total correcto) */
        onTotalChanged(calcularTotal())
    }

    /**
     * Actualiza la lista completa de hilos y notifica al adaptador para refrescar la vista.
     *
     * @param nuevaLista Nueva lista de hilos que sustituye a la actual.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<HiloGrafico>) {
        hilos.clear()
        hilos.addAll(nuevaLista)
        notifyDataSetChanged()
        onTotalChanged(calcularTotal())
    }

    /**
     * Establece el hilo que debe ser resaltado en la lista y refresca el adaptador.
     *
     * @param hiloId Identificador (nombre) del hilo a resaltar o null para quitar resaltado.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }

    /**
     * Calcula el total de madejas sumando las cantidades modificadas
     * si existen, o la cantidad original si no.
     *
     * @return Total de madejas a pedido.
     */
    private fun calcularTotal(): Int {
        return hilos.sumOf { it.cantidadModificar ?: it.madejas }
    }
}
