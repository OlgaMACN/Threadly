package logica.stock_personal

import android.content.Context
import com.threadly.R
import utiles.funciones.leerCodigoHilo

/* esta clase sirve para que haya una única instancia de la lista, accesible desde cualquier otra clase */
object StockSingleton {
    var listaStock: MutableList<HiloStock> = mutableListOf()

    fun obtenerMadejas(hiloId: String): Int? {
        return listaStock.find { it.hiloId == hiloId }?.madejas
    }

    fun agregarHilo(hilo: String, madejas: Int) {
        listaStock.add(HiloStock(hilo, madejas))
    }

    fun inicializarStockSiNecesario(context: Context) {
        if (listaStock.isEmpty()) {
            listaStock = leerCodigoHilo(context, R.raw.catalogo_hilos)
        }
    }

    /* uso de shared preferences para comprobar/ guardar info sobre si es la primera vez */
    fun esPrimeraVez(context: Context): Boolean {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        return prefs.getBoolean("primera_vez", true)
    }

    fun marcarPrimeraVez(context: Context) {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("primera_vez", false).apply()
    }
}