package com.tuapp.tunombre

import stock_personal.StockItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class StockAdapter(private val listaStock: MutableList<StockItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_CABECERA = 0
        private const val TIPO_ITEM = 1
    }

    // ¿Qué tipo de layout usar para cada posición?
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TIPO_CABECERA else TIPO_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_CABECERA) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.stock_tabla_filas_cabecera, parent, false)
            CabeceraViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.stock_tabla_filas_contenido, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return listaStock.size + 1 // +1 para la cabecera
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder && position > 0) {
            val item = listaStock[position - 1]
            holder.bind(item)
        }
        // No hace falta bindear cabecera si ya está fija
    }

    // ViewHolder para la cabecera
    inner class CabeceraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ViewHolder para las filas normales
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numHilo: TextView = itemView.findViewById(R.id.txtVw_hiloID)
        private val numMadejas: TextView = itemView.findViewById(R.id.txtVw_numeroMadejasTabla)

        fun bind(stockItem: StockItem) {
            numHilo.text = stockItem.numeroHilo
            numMadejas.text = stockItem.numeroMadejas.toString()

            // Aquí puedes hacer que al pulsar abra un diálogo
            itemView.setOnClickListener {
                // abrirDialogo(stockItem) o como quieras
            }
        }
    }

    // Método para agregar un nuevo hilo a la lista
    fun agregarHilo(stockItem: StockItem) {
        listaStock.add(stockItem)
        notifyItemInserted(listaStock.size) // Porque la cabecera es la posición 0
    }
}
