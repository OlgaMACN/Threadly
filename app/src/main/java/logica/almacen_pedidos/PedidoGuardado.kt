package logica.almacen_pedidos

import logica.pedido_hilos.Grafico
import java.io.Serializable

/**
 * Clase de datos que representa un pedido guardado en el almacenamiento local.
 *
 * Un pedido está compuesto por:
 * - Un nombre que lo identifica (por ejemplo, "P2405_1").
 * - Una lista de gráficos asociados, donde cada gráfico tiene sus propios hilos.
 * - Un estado booleano que indica si el pedido ya ha sido realizado (es decir, si se ha actualizado el stock).
 *
 * Esta clase implementa Serializable para permitir su paso entre actividades mediante Intents.
 *
 * @property nombre Nombre único e identificador del pedido guardado.
 * @property graficos Lista de gráficos incluidos en el pedido.
 * @property realizado Indica si el pedido ha sido marcado como completado o no.
 * * @author Olga y Sandra Macías Aragón
 *
 */
data class PedidoGuardado(
    val nombre: String,
    val graficos: List<Grafico>,
    var realizado: Boolean = false
) : Serializable /* implementa Serializable para enviar entre Activities */
