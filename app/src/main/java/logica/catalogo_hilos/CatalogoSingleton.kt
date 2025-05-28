package logica.catalogo_hilos

import utiles.funciones.ordenarHilos

/**
 * Singleton que gestiona el catálogo completo de hilos en la aplicación Threadly.
 * Contiene la lista mutable de hilos y proporciona métodos para cargar,
 * agregar, modificar y eliminar hilos, manteniendo la lista ordenada por número de hilo.
 *
 * Esta clase sólo maneja datos y operaciones en la lista, sin interacción directa con la UI.
 *
 * * @author Olga y Sandra Macías Aragón
 */
object CatalogoSingleton {

    /**
     * Lista mutable que almacena todos los hilos del catálogo.
     * Se mantiene ordenada por número de hilo tras cada modificación.
     */
    var listaCatalogo = mutableListOf<HiloCatalogo>()

    /**
     * Carga una lista completa de hilos en el catálogo, reemplazando la lista actual.
     * Ordena automáticamente la lista por número de hilo.
     *
     * @param lista Lista de hilos a cargar.
     */
    fun cargarLista(lista: List<HiloCatalogo>) {
        listaCatalogo = ordenarHilos(lista) { it.numHilo }.toMutableList()
    }

    /**
     * Añade un nuevo hilo al catálogo si no existe otro con el mismo número (ignorando mayúsculas/minúsculas).
     * Tras añadir, ordena la lista por número de hilo.
     *
     * @param hilo Nuevo objeto [HiloCatalogo] a agregar.
     * @return `true` si se agregó correctamente, `false` si ya existía un hilo con ese número.
     */
    fun agregarHilo(hilo: HiloCatalogo): Boolean {
        if (listaCatalogo.any { it.numHilo.equals(hilo.numHilo, ignoreCase = true) }) return false
        listaCatalogo.add(hilo)
        listaCatalogo = ordenarHilos(listaCatalogo) { it.numHilo }.toMutableList()
        return true
    }

    /**
     * Modifica un hilo existente en una posición dada, actualizando su número y/o nombre.
     * Antes de cambiar el número, verifica que no exista otro hilo con ese número.
     * Ordena la lista tras la modificación.
     *
     * @param posicion Índice del hilo a modificar en la lista.
     * @param nuevoNum Nuevo número para el hilo, o null para no cambiarlo.
     * @param nuevoNombre Nuevo nombre para el hilo, o null para no cambiarlo.
     * @return `true` si la modificación fue exitosa, `false` si el nuevo número ya existe.
     */
    fun modificarHilo(
        posicion: Int,
        nuevoNum: String?,
        nuevoNombre: String?
    ): Boolean {
        val hiloActual = listaCatalogo[posicion]

        if (nuevoNum != null && !nuevoNum.equals(hiloActual.numHilo, ignoreCase = true)) {
            if (listaCatalogo.any { it.numHilo.equals(nuevoNum, ignoreCase = true) }) return false
            hiloActual.numHilo = nuevoNum
        }

        if (nuevoNombre != null) {
            hiloActual.nombreHilo = nuevoNombre
        }

        listaCatalogo = ordenarHilos(listaCatalogo) { it.numHilo }.toMutableList()
        return true
    }

    /**
     * Elimina el hilo ubicado en la posición indicada de la lista.
     *
     * @param posicion Índice del hilo a eliminar.
     */
    fun eliminarHilo(posicion: Int) {
        listaCatalogo.removeAt(posicion)
    }
}
