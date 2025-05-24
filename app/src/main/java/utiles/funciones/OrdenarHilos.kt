package utiles.funciones

/* función para ordenar los hilos en la tabla */
fun <T> ordenarHilos(listaHilos: List<T>, selector: (T) -> String): List<T> {
    return listaHilos.sortedWith(compareBy(
        { hilo ->
            val hiloStr = selector(hilo)
            when {
                hiloStr.matches(Regex("^[A-Za-z]+$")) -> 0
                hiloStr.matches(Regex("^[0-9]+$")) -> 2
                else -> 1
            }
        },
        { hilo ->
            Regex("^([A-Za-z/]+)").find(selector(hilo))?.value ?: ""
        },
        { hilo ->
            Regex("(\\d+)").find(selector(hilo))?.value?.toIntOrNull() ?: 0
        }
    ))
}
