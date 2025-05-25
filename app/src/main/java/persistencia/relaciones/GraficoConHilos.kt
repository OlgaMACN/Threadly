package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Grafico
import persistencia.entidades.GraficoHilo

/**
 * Representa la relación entre un [Grafico] y sus [GraficoHilo].
 *
 * Esta clase permite obtener un gráfico junto con la lista de hilos asociados a dicho gráfico
 * mediante la anotación [Relation] de Room.
 *
 * @property grafico El objeto [Grafico] embebido.
 * @property hilos La lista de objetos [GraficoHilo] relacionados con el gráfico.
 */
data class GraficoConHilos(
    @Embedded val grafico: Grafico,

    @Relation(
        parentColumn = "graphicId",
        entityColumn = "graphicId"
    )
    val hilos: List<GraficoHilo>
)
