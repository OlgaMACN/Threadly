package logica.grafico_pedido

import java.io.Serializable

/**
 * Representa un hilo específico dentro de un gráfico, incluyendo la cantidad
 * de madejas necesarias y una posible cantidad modificada para pedidos.
 *
 * Esta clase se usa en la pantalla de edición de gráficos de pedidos.
 *
 * @property hilo Identificador único del hilo (por ejemplo, su código o nombre).
 * @property madejas Cantidad de madejas necesarias originalmente para el gráfico.
 * @property cantidadModificar Cantidad modificada introducida por el usuario para este hilo.
 *                              Si es null, se usará la cantidad original [madejas].
 *
 * Implementa [Serializable] para poder ser enviada entre actividades mediante Intents.
 */
data class HiloGrafico(
    val hilo: String,
    val madejas: Int,
    var cantidadModificar: Int? = null
) : Serializable
