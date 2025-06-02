package persistencia.entidades

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Un Pedido con sus Gráficos “en cascada”.
 */
data class PedidoConGraficos(
    @Embedded
    val pedido: PedidoEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "idPedido",    // campo en GraficoEntity que apunta a PedidoEntity.id
        entity = GraficoEntity::class
    )
    val graficos: List<GraficoConHilos>
)
