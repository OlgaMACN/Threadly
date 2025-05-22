package logica.stock_personal

import android.content.Context
import com.threadly.R
import utiles.funciones.LeerXMLCodigo

/* Esta clase sirve para que haya una única instancia de la lista, accesible desde cualquier otra clase, con todas sus funciones */
object StockSingleton {
    var listaStock: MutableList<HiloStock> = mutableListOf()

    fun obtenerMadejas(hiloId: String): Int? {
        return listaStock.find { it.hiloId == hiloId }?.madejas
    }

    fun mostrarTotalStock(): Int {
        return listaStock.sumOf { it.madejas }
    }

    /* Inicializa el stock solo si está vacío, cargando desde el archivo del catálogo */
    fun inicializarStockSiNecesario(context: Context) {
        if (listaStock.isEmpty()) {
            listaStock = LeerXMLCodigo(context, R.raw.catalogo_hilos)
        }
    }

    /* Uso de SharedPreferences para comprobar / guardar info sobre si es la primera vez */
    fun esPrimeraVez(context: Context): Boolean {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        return prefs.getBoolean("primera_vez", true)
    }

    fun marcarPrimeraVez(context: Context) {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("primera_vez", false).apply()
    }
}
