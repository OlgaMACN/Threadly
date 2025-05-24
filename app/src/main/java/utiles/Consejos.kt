package utiles

/* no es una entidad persistente (no se va a guardar ni modificar en la base de datos), no necesita data class ni DAO.
De esta manera estará disponible de forma global y accesible desde cualquier parte de la app.*/
object Consejos {
    val lista = listOf(
        "Elige una aguja adecuada para tu hilo.",
        "Mantén el bastidor bien tenso.",
        "Lava tus manos antes de bordar.",
        "Evita trabajar con hilos muy largos.",
        "Descansa cada cierto tiempo para no forzar la vista."
    )

    fun obtenerAleatorio(): String {
        return lista.random()
    }
}