package utiles

/**
 * Objeto singleton que contiene una lista fija de consejos útiles para el bordado.
 *
 * No es una entidad persistente, por lo que no se guarda ni modifica en la base de datos.
 * Está diseñado para estar disponible globalmente y accesible desde cualquier parte de la aplicación.
 */
object Consejos {

    /**
     * Lista inmutable de consejos relacionados con la práctica del bordado.
     */
    val lista = listOf(
        "Elige una aguja adecuada para tu hilo.",
        "Mantén el bastidor bien tenso.",
        "Lava tus manos antes de bordar.",
        "Evita trabajar con hilos muy largos.",
        "Descansa cada cierto tiempo para no forzar la vista."
    )

    /**
     * Devuelve un consejo aleatorio de la lista.
     *
     * @return Un String con un consejo seleccionado al azar.
     */
    fun obtenerAleatorio(): String {
        return lista.random()
    }
}
