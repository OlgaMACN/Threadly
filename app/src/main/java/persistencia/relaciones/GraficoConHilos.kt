package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.GraficoPedidoEntity
import persistencia.entidades.HiloGraficoEntity

data class GraficoConHilos(
    @Embedded val grafico: GraficoPedidoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "graficoId"
    )
    val hilos: List<HiloGraficoEntity>
)
