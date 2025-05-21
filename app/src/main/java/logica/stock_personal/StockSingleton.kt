package logica.stock_personal

import android.content.Context
import com.threadly.R
import persistencia.bbdd.StockBdD
import utiles.funciones.leerCodigoHilo

/* esta clase sirve para que haya una única instancia de la lista, accesible desde cualquier otra clase, con todas sus funciones */
object StockSingleton {
    var listaStock: MutableList<HiloStock> = mutableListOf()

    fun obtenerMadejas(hiloId: String): Int? {
        return listaStock.find { it.hiloId == hiloId }?.madejas
    }

    fun mostrarTotalStock(): Int {
        return listaStock.sumOf { it.madejas }
    }

    /* al ser suspend hay que llamarla con coroutine, es asíncrona */
    suspend fun actualizarDesdeBaseDeDatos(context: Context) {
        val dao = StockBdD.getDatabase(context).hiloStockDao()
        val listaActualizada = dao.obtenerTodos().map {
            HiloStock(it.hiloId, it.madejas)
        }
        listaStock.clear()
        listaStock.addAll(listaActualizada)
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