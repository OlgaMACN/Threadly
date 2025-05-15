package grafico_pedido

import java.io.Serializable

data class HiloGrafico(
    val hilo: String,
    val puntadas: Int,
    val madejas: Int
) : Serializable
