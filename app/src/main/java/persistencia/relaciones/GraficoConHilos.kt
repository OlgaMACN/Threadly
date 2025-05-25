package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.GraficoEntidad
import persistencia.entidades.GraficoHilo

/**
 * Representa la relación entre un [GraficoEntidad] y sus [GraficoHilo].
 *
 * Esta clase permite obtener un gráfico junto con la lista de hilos asociados a dicho gráfico
 * mediante la anotación [Relation] de Room.
 *
 * @property grafico El objeto [GraficoEntidad] embebido.
 * @property hilos La lista de objetos [GraficoHilo] relacionados con el gráfico.
 */
data class GraficoConHilos(
    @Embedded val grafico: GraficoEntidad,

    @Relation(
        parentColumn = "graphicId",
        entityColumn = "graphicId"
    )
    val hilos: List<GraficoHilo>
)
