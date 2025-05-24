package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Grafico
import persistencia.entidades.GraficoHilo

data class GraficoConHilos(
    @Embedded val grafico: Grafico,

    @Relation(
        parentColumn = "graficoId",
        entityColumn = "graficoId"
    )
    val hilos: List<GraficoHilo>
)
