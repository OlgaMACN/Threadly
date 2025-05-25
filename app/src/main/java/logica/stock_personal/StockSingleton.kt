package logica.stock_personal

import android.content.Context
import android.widget.Toast
import com.threadly.R
import utiles.funciones.LeerXMLCodigo

/**
 * Objeto singleton que gestiona el inventario personal de hilos del usuario.
 *
 * Proporciona funciones para inicializar, consultar, agregar, modificar y eliminar hilos del stock.
 * Además, permite manejar un estado persistente para saber si es la primera vez que se accede al stock.
 *
 * El stock se representa como una lista mutable de objetos [HiloStock].
 */
object StockSingleton {

    /* lista que contiene todos los hilos disponibles en el stock del usuario. */
    var listaStock: MutableList<HiloStock> = mutableListOf()

    /**
     * Obtiene el número de madejas disponibles de un hilo específico.
     *
     * @param hiloId Identificador del hilo (ignorando mayúsculas/minúsculas).
     * @return Número de madejas o null si el hilo no está en el stock.
     */
    fun obtenerMadejas(hiloId: String): Int? {
        return listaStock.find { it.hiloId.equals(hiloId, ignoreCase = true) }?.madejas
    }

    /**
     * Calcula el total de madejas disponibles sumando todas las del stock.
     *
     * @return Total de madejas en el inventario.
     */
    fun mostrarTotalStock(): Int {
        return listaStock.sumOf { it.madejas }
    }

    /**
     * Inicializa el stock si aún no hay datos cargados.
     * Lee el XML predefinido de hilos desde recursos si la lista está vacía.
     *
     * @param context Contexto de la aplicación necesario para acceder a recursos.
     */
    fun inicializarStockSiNecesario(context: Context) {
        if (listaStock.isEmpty()) {
            listaStock = LeerXMLCodigo(context, R.raw.catalogo_hilos)
        }
    }

    /**
     * Agrega un nuevo hilo al stock si no existe ya.
     *
     * @param hiloId Identificador del hilo.
     * @param madejas Cantidad inicial de madejas (debe ser >= 0).
     * @return true si se agregó correctamente, false si ya existía o los datos no son válidos.
     */
    fun agregarHilo(hiloId: String, madejas: Int): Boolean {
        if (hiloId.isBlank() || madejas < 0) return false
        if (existeHilo(hiloId)) return false
        listaStock.add(HiloStock(hiloId.uppercase(), madejas))
        return true
    }

    /**
     * Verifica si un hilo ya existe en el stock.
     *
     * @param hiloId Identificador del hilo.
     * @return true si ya está registrado, false en caso contrario.
     */
    fun existeHilo(hiloId: String): Boolean {
        return listaStock.any { it.hiloId.equals(hiloId, ignoreCase = true) }
    }

    /**
     * Suma una cantidad de madejas a un hilo ya existente.
     *
     * @param hiloId Identificador del hilo.
     * @param cantidad Número de madejas a agregar.
     * @return true si se realizó correctamente, false si el hilo no existe.
     */
    fun agregarMadejas(hiloId: String, cantidad: Int): Boolean {
        val index = listaStock.indexOfFirst { it.hiloId.equals(hiloId, ignoreCase = true) }
        if (index == -1) return false // No existe el hilo
        val actual = listaStock[index]
        listaStock[index] = actual.copy(madejas = actual.madejas + cantidad)
        return true
    }

    /**
     * Modifica directamente la cantidad de madejas de un hilo (sobrescribe la cantidad).
     *
     * @param hiloId Identificador del hilo.
     * @param nuevaCantidad Nueva cantidad de madejas (debe ser >= 0).
     * @return true si se modificó correctamente, false si el hilo no existe o el número es inválido.
     */
    fun modificarMadejas(hiloId: String, nuevaCantidad: Int): Boolean {
        if (nuevaCantidad < 0) return false
        val index = listaStock.indexOfFirst { it.hiloId.equals(hiloId, ignoreCase = true) }
        if (index == -1) return false
        val actual = listaStock[index]
        listaStock[index] = actual.copy(madejas = nuevaCantidad)
        return true
    }

    /**
     * Elimina un hilo del stock.
     *
     * @param hiloId Identificador del hilo a eliminar.
     * @return true si se eliminó correctamente, false si no se encontró.
     */
    fun eliminarHilo(hiloId: String): Boolean {
        return listaStock.removeIf { it.hiloId.equals(hiloId, ignoreCase = true) }
    }

    /**
     * Verifica si es la primera vez que el usuario accede al stock.
     * Se usa para mostrar tutoriales, precargar datos, etc.
     *
     * @param context Contexto de la aplicación.
     * @return true si es la primera vez, false si ya se accedió anteriormente.
     */
    fun esPrimeraVez(context: Context): Boolean {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        return prefs.getBoolean("primera_vez", true)
    }

    /**
     * Marca que el usuario ya ha accedido al stock una vez.
     *
     * @param context Contexto de la aplicación.
     */
    fun marcarPrimeraVez(context: Context) {
        val prefs = context.getSharedPreferences("prefs_stock", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("primera_vez", false).apply()
    }
}
