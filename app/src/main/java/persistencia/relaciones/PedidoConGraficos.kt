package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Grafico
import persistencia.entidades.Pedido

data class PedidoConGraficos(
    @Embedded val pedido: Pedido,

    @Relation(
        parentColumn = "pedidoId",
        entityColumn = "pedidoId"
    )
    val graficos: List<Grafico>
)
