package CatalogoHilos

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import stock_personal.AdaptadorStock
import stock_personal.HiloStock

class AdaptadorCatalogo(
    private var items: MutableList<HiloStock> = mutableListOf(), /* mutable porque la tabla es cambiante */
    private val onLongClick: (Int) -> Unit,
    private var hiloResaltado: String? = null /* para el hilo encontrado mediante el buscador */
) : RecyclerView.Adapter<AdaptadorStock.StockViewHolder>() {

    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {


    }
}