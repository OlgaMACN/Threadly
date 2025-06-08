package utiles.funciones

/**
 * Calcula cuántas madejas de hilo se necesitan para realizar un bordado,
 * en función del número de puntadas y del count (densidad) de la tela.
 *
 * Esta fórmula se basa en una estimación media de longitud consumida por puntada
 * y la longitud estándar de una madeja (aproximadamente 2.66 metros).
 *
 * @param puntadas Número total de puntadas del gráfico.
 * @param countTela Densidad de la tela (count), es decir, número de hilos por pulgada.
 *                  Valores típicos: 14, 16, 18, 20, 25...
 * @return Número estimado de madejas necesarias.
 *
 * ### Ejemplo de uso:
 * ```
 * val madejas = calcularMadejas(puntadas = 1200, countTela = 16)
 * ```
 *
 * @author Olga y Sandra Macías Aragón
 */
fun calcularMadejas(puntadas: Int, countTela: Int): Int {
    /* se define la longitud media consumida por puntada según el count de la tela */
    val longitudPorPuntada = when (countTela) {
        14 -> 0.0076
        16 -> 0.0068
        18 -> 0.0061
        20 -> 0.0056
        25 -> 0.0045
        else -> 0.0076 /* si no se especifica un valor válido, se usa el estándar de 14 count */
    }

    /* longitud media de una madeja (en metros) */
    val longitudMedia = 2.66

    /* se calcula la longitud total estimada de hilo necesaria */
    val totalLongitud = puntadas * longitudPorPuntada

    /* se devuelve el número total de madejas redondeado hacia arriba */
    return kotlin.math.ceil(totalLongitud / longitudMedia).toInt()
}
