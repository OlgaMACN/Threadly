package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Pedido

data class PedidoConGraficosYHilos(
    @Embedded val pedido: Pedido,

    @Relation(
        entity = persistencia.entidades.Grafico::class,
        parentColumn = "pedidoId",
        entityColumn = "pedidoId"
    )
    val graficosConHilos: List<GraficoConHilos>
)
