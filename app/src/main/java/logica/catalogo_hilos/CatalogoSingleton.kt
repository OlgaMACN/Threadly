package logica.catalogo_hilos

import utiles.funciones.ordenarHilos
/* Esta clase sólo maneja datos y las operaciones en la lista */
object CatalogoSingleton {
    var listaCatalogo = mutableListOf<HiloCatalogo>()

    fun cargarLista(lista: List<HiloCatalogo>) {
        listaCatalogo = ordenarHilos(lista) { it.numHilo }.toMutableList()
    }

    fun agregarHilo(hilo: HiloCatalogo): Boolean {
        if (listaCatalogo.any { it.numHilo.equals(hilo.numHilo, ignoreCase = true) }) return false
        listaCatalogo.add(hilo)
        listaCatalogo = ordenarHilos(listaCatalogo) { it.numHilo }.toMutableList()
        return true
    }

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

    fun eliminarHilo(posicion: Int) {
        listaCatalogo.removeAt(posicion)
    }
}
