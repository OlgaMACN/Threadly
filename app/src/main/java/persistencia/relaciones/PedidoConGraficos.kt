package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.GraficoPedidoEntity
import persistencia.entidades.PedidoEntity


data class PedidoConGraficos(
    @Embedded val pedido: PedidoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pedidoId",
        entity = GraficoPedidoEntity::class
    )
    val graficos: List<GraficoConHilos>
)
