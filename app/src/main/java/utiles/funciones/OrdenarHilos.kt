package utiles.funciones

/**
 * Ordena una lista de elementos genéricos [T] según un criterio personalizado de texto,
 * pensado para ordenar códigos de hilos de forma alfanumérica natural.
 *
 * La lógica de ordenación se basa en tres niveles:
 * 1. Letras puras (ej. "BLANC"), luego combinaciones alfanuméricas (ej. "B5200"), y por último números puros (ej. "310").
 * 2. Orden alfabético de la parte de texto inicial.
 * 3. Orden numérico del número contenido, si lo hay.
 *
 * Este comportamiento simula la ordenación humana que se esperaría en listas como catálogos de hilos.
 *
 * @param T El tipo de los elementos de la lista.
 * @param listaHilos La lista de elementos a ordenar.
 * @param selector Función lambda que extrae el valor de tipo `String` que se usará para ordenar.
 * @return Una nueva lista con los elementos ordenados según el criterio descrito.
 *
 * ### Ejemplo de uso:
 * ```kotlin
 * ordenarHilos(lista) { it.numHilo }
 * ```
 * Donde `numHilo` puede ser "B5200", "310", "BLANC", etc.
 *
 * @author Olga y Sandra Macías Aragón
 */
fun <T> ordenarHilos(listaHilos: List<T>, selector: (T) -> String): List<T> {
    return listaHilos.sortedWith(compareBy(
        { hilo ->
            val hiloStr = selector(hilo)
            when {
                hiloStr.matches(Regex("^[A-Za-z]+$")) -> 0 /* solo letras */
                hiloStr.matches(Regex("^[0-9]+$")) -> 2     /* solo números */
                else -> 1 /* combinación o formato especial */
            }
        },
        { hilo ->
            /* parte alfabética inicial para ordenar por letras primero */
            Regex("^([A-Za-z/]+)").find(selector(hilo))?.value ?: ""
        },
        { hilo ->
            /* número dentro del código, para orden numérico real */
            Regex("(\\d+)").find(selector(hilo))?.value?.toIntOrNull() ?: 0
        }
    ))
}
