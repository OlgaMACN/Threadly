package persistencia.entidades

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relación uno-a-muchos entre un gráfico y sus hilos asociados.
 *
 * Esta clase representa un gráfico (`GraficoEntity`) junto con todos los hilos (`HiloGraficoEntity`)
 * que se han añadido a ese gráfico. Se utiliza como entidad anidada dentro de relaciones más grandes,
 * como por ejemplo en un pedido completo con múltiples gráficos y sus respectivos hilos.
 *
 * Esta estructura es útil para consultar los datos de un gráfico completo en una única operación Room.
 *
 * IMPORTANTE: Se usa en la relación `PedidoConGraficos`.
 *
 * @property grafico Entidad principal que representa el gráfico.
 * @property hilos Lista de hilos asignados al gráfico, relacionados por `graficoId`.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
data class GraficoConHilos(
    @Embedded
    val grafico: GraficoEntity,

    @Relation(
        parentColumn = "id",             /* columna de referencia en GraficoEntity */
        entityColumn = "graficoId",      /* clave foránea en HiloGraficoEntity */
        entity = HiloGraficoEntity::class
    )
    val hilos: List<HiloGraficoEntity>
)
