package persistencia.entidades

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Un gráfico con sus hilos. Se anida dentro de PedidoConGraficos.
 */
data class GraficoConHilos(
    @Embedded
    val grafico: GraficoEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "graficoId",
        entity = HiloGraficoEntity::class
    )
    val hilos: List<HiloGraficoEntity>
)
