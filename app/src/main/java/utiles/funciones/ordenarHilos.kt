package utiles.funciones

import logica.grafico_pedido.HiloGrafico

/* función para ordenar los hilos en la tabla */
fun ordenarHilos(listaHilos: List<HiloGrafico>): List<HiloGrafico> {
    return listaHilos.sortedWith(compareBy(
        { hilo ->
            /* primero los que son sólo letras: 0, luego alfanuméricos: 1 y finalmente sólo números: 1*/
            when {
                hilo.hilo.matches(Regex("^[A-Za-z]+$")) -> 0
                hilo.hilo.matches(Regex("^[0-9]+$")) -> 2
                else -> 1
            }
        },
        { hilo ->
            /* primero se ordena por letras, si hay */
            Regex("^([A-Za-z/]+)").find(hilo.hilo)?.value ?: ""
        },
        { hilo ->
            /* después, se ordena por número */
            Regex("(\\d+)").find(hilo.hilo)?.value?.toIntOrNull() ?: 0
        }
    ))
}
