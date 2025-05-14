package utiles

fun calcularMadejas(puntadas: Int, countTela: Int): Int {
    /* con una estructura de switch se establecen los valores según cada count */
    val longitudPorPuntada = when (countTela) {
        14 -> 0.0076
        16 -> 0.0068
        18 -> 0.0061
        20 -> 0.0056
        25 -> 0.0045
        else -> 0.0076 /* por defecto será count 14, que es la más común */
    }

    /* fórmula para calcular las madejas a partir del count de la tela y el número de puntadas */
    val longitudUtilMadeja = 2.66
    val totalLongitud = puntadas * longitudPorPuntada
    return kotlin.math.ceil(totalLongitud / longitudUtilMadeja).toInt()
}
