package logica.grafico_pedido

import java.io.Serializable

data class HiloGrafico(
    val hilo: String,
    val madejas: Int,
    var cantidadModificar: Int? = null
) : Serializable


