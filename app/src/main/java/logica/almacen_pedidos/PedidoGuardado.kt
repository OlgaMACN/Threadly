package logica.almacen_pedidos

import logica.pedido_hilos.Grafico
import persistencia.entidades.PedidoEntity
import java.io.Serializable

data class PedidoGuardado(
    val id: Int,
    val nombre: String,
    val userId: Int,                  // <-- agregamos userId aquí
    var realizado: Boolean = false,
    val graficos: List<Grafico>
) : Serializable

fun PedidoGuardado.toEntity(): PedidoEntity {
    return PedidoEntity(
        id = id,
        nombre = nombre,
        userId = userId,
        realizado = realizado
    )
}

fun PedidoEntity.toPedidoGuardado(): PedidoGuardado {
    return PedidoGuardado(
        id = id,
        nombre = nombre,
        userId = userId,
        realizado = realizado,
        graficos = mutableListOf()
    )
}
