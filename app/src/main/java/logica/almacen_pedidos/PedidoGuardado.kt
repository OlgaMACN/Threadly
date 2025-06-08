package logica.almacen_pedidos

import logica.pedido_hilos.Grafico
import persistencia.entidades.PedidoEntity
import java.io.Serializable

/**
 * Clase de datos que representa un pedido guardado completo, incluyendo su lista de gráficos.
 *
 * Se utiliza en la capa lógica y de interfaz, y puede convertirse a/desde [PedidoEntity]
 * para persistencia en Room.
 *
 * @property id        Identificador único del pedido.
 * @property nombre    Nombre asignado al pedido.
 * @property userId    ID del usuario propietario del pedido.
 * @property realizado Indica si el pedido ha sido marcado como realizado.
 * @property graficos  Lista de gráficos (con hilos) que forman el pedido.
 *
 * @see PedidoEntity   Versión persistente del pedido.
 * @see toEntity       Conversión desde [PedidoGuardado] a [PedidoEntity].
 * @see toPedidoGuardado Conversión desde [PedidoEntity] a [PedidoGuardado].
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
data class PedidoGuardado(
    val id: Int,
    val nombre: String,
    val userId: Int,
    var realizado: Boolean = false,
    val graficos: List<Grafico>
) : Serializable

/**
 * Convierte un [PedidoGuardado] a su entidad persistente [PedidoEntity].
 *
 * Los gráficos no se incluyen aquí, ya que se almacenan por separado en otras tablas relacionadas.
 */
fun PedidoGuardado.toEntity(): PedidoEntity {
    return PedidoEntity(
        id = id,
        nombre = nombre,
        userId = userId,
        realizado = realizado
    )
}

/**
 * Convierte una entidad [PedidoEntity] a un [PedidoGuardado] vacío (sin gráficos).
 *
 * Los gráficos se cargan posteriormente mediante relaciones Room.
 */
fun PedidoEntity.toPedidoGuardado(): PedidoGuardado {
    return PedidoGuardado(
        id = id,
        nombre = nombre,
        userId = userId,
        realizado = realizado,
        graficos = mutableListOf() /* se asignarán después desde la relación */
    )
}
