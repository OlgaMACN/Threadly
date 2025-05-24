package logica.stock_personal

import android.content.Context
import android.widget.Toast
import com.threadly.R
import utiles.funciones.LeerXMLCodigo

object StockSingleton {
    var listaStock: MutableList<HiloStock> = mutableListOf()

    fun obtenerMadejas(hiloId: String): Int? {
        return listaStock.find { it.hiloId.equals(hiloId, ignoreCase = true) }?.madejas
    }

    fun mostrarTotalStock(): Int {
        return listaStock.sumOf { it.madejas }
    }

    fun inicializarStockSiNecesario(context: Context) {
        if (listaStock.isEmpty()) {
            listaStock = LeerXMLCodigo(context, R.raw.catalogo_hilos)
        }
    }
    /* Agregar un nuevo hilo al stock (solo si no existe ya) */
    fun agregarHilo(hiloId: String, madejas: Int): Boolean {
        if (hiloId.isBlank() || madejas < 0) return false
        if (existeHilo(hiloId)) return false
        listaStock.add(HiloStock(hiloId.uppercase(), madejas))
        return true
    }

    /* Verifica si el hilo existe en el stock */
    fun existeHilo(hiloId: String): Boolean {
        return listaStock.any { it.hiloId.equals(hiloId, ignoreCase = true) }
    }

    /* Agregar madejas a un hilo existente */
    fun agregarMadejas(hiloId: String, cantidad: Int): Boolean {
        val index = listaStock.indexOfFirst { it.hiloId.equals(hiloId, ignoreCase = true) }
        if (index == -1) return false // No existe el hilo
        val actual = listaStock[index]
        listaStock[index] = actual.copy(madejas = actual.madejas + cantidad)
        return true
    }

    /* Modificar madejas, con validación (no negativas) */
    fun modificarMadejas(hiloId: String, nuevaCantidad: Int): Boolean {
        if (nuevaCantidad < 0) return false
        val index = listaStock.indexOfFirst { it.hiloId.equals(hiloId, ignoreCase = true) }
        if (index == -1) return false
        val actual = listaStock[index]
        listaStock[index] = actual.copy(madejas = nuevaCantidad)
        return true
    }

    /* Eliminar hilo del stock */
    fun eliminarHilo(hiloId: String): Boolean {
        return listaStock.removeIf { it.hiloId.equals(hiloId, ignoreCase = true) }
    }

    /* Para SharedPreferences, como tienes */
    fun esPrimeraVez(context: Context): Boolean {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        return prefs.getBoolean("primera_vez", true)
    }

    fun marcarPrimeraVez(context: Context) {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("primera_vez", false).apply()
    }
}
