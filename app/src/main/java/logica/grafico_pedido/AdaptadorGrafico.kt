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
 * Permite:
 *  - Mostrar el código de cada hilo y sus madejas originales.
 *  - Dejar al usuario introducir una nueva cantidad (cantidadModificar) para cada hilo.
 *  - Resaltar un hilo especificado.
 *  - Notificar, mediante [onTotalChanged], el total actual de madejas (sumando las modificadas).
 *
 * @param hilos Lista mutable de [HiloGrafico] que representa los hilos del gráfico.
 * @param onClickHilo Lambda que se ejecuta al pulsar el TextView del hilo.
 * @param onLongClickHilo Lambda opcional para pulsación larga (ej. eliminar).
 * @param hiloResaltado Código del hilo que debe resaltarse (nombre), o null para ninguno.
 * @param onTotalChanged Lambda que recibe el total de madejas (Int), se llama cuando cambia el total.
 */
class AdaptadorGrafico(
    private val hilos: MutableList<HiloGrafico>,
    private val onClickHilo: (HiloGrafico) -> Unit,
    private val onLongClickHilo: ((HiloGrafico) -> Unit)? = null,
    private var hiloResaltado: String? = null,
    private val onTotalChanged: (Int) -> Unit
) : RecyclerView.Adapter<AdaptadorGrafico.HiloViewHolder>() {

    /**
     * ViewHolder que contiene las vistas de una fila:
     *  - txtHilo: muestra el código/identificador del hilo.
     *  - txtMadejas: muestra las madejas originales del hilo.
     *  - edtModificar: EditText donde el usuario puede poner una nueva cantidad.
     *  - filaLayout: root view de la fila, para cambiar el fondo (resaltar).
     *  - textWatcher: referencia al TextWatcher para poder retirarlo antes de asignar uno nuevo.
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
        val hiloItem = hilos[position]

        // 1) Mostrar código y madejas originales
        holder.txtHilo.text = hiloItem.hilo
        holder.txtMadejas.text = hiloItem.madejas.toString()

        // 2) Retirar cualquier TextWatcher previo para evitar duplicados
        holder.textWatcher?.let {
            holder.edtModificar.removeTextChangedListener(it)
        }
        // 3) Mostrar, si existe, la cantidad modificada; si no, dejar vacío
        holder.edtModificar.setText(hiloItem.cantidadModificar?.toString() ?: "")

        // 4) Crear y asignar un nuevo TextWatcher que actualiza cantidadModificar y total
        holder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s?.toString().orEmpty()
                hiloItem.cantidadModificar = texto.toIntOrNull()
                // Notificar total sólo aquí, pues el usuario cambió la cantidad
                onTotalChanged(calcularTotal())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.edtModificar.addTextChangedListener(holder.textWatcher)

        // 5) Resaltar fila si coincide con hiloResaltado
        if (hiloItem.hilo == hiloResaltado) {
            holder.filaLayout.setBackgroundResource(R.drawable.reutilizable_resaltar_busqueda)
        } else {
            holder.filaLayout.setBackgroundResource(android.R.color.transparent)
        }

        // 6) Clic simple sobre el nombre del hilo
        holder.txtHilo.setOnClickListener {
            onClickHilo(hiloItem)
        }
        // 7) Pulsación larga en toda la fila para invocar onLongClickHilo
        holder.itemView.setOnLongClickListener {
            onLongClickHilo?.invoke(hiloItem)
            true
        }

    }

    /**
     * Reemplaza la lista interna de hilos por [nueva], notifica el cambio y recalcula total.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nueva: List<HiloGrafico>) {
        hilos.clear()
        hilos.addAll(nueva)
        notifyDataSetChanged()
        // Recalcular el total inmediatamente tras cambiar la lista completa
        onTotalChanged(calcularTotal())
    }

    /**
     * Marca el [hiloId] para resaltarlo visualmente. Llama a notifyDataSetChanged().
     */
    @SuppressLint("NotifyDataSetChanged")
    fun resaltarHilo(hiloId: String?) {
        hiloResaltado = hiloId
        notifyDataSetChanged()
    }

    /**
     * Devuelve la lista interna de [HiloGrafico] que está mostrando el adaptador.
     * Útil para que la actividad/fragmento lea la lista al devolver resultado o buscar.
     */
    fun obtenerLista(): MutableList<HiloGrafico> = hilos

    /**
     * Suma la cantidad final para cada hilo:
     *   - Si [cantidadModificar] no es null, se usa ese valor.
     *   - En caso contrario, se usa la propiedad [madejas].
     */
    private fun calcularTotal(): Int {
        return hilos.sumOf { hilo ->
            hilo.cantidadModificar ?: hilo.madejas
        }
    }
}
