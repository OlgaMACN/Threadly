package persistencia.entidades

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Un Pedido con sus Gráficos “en cascada”.
 *
 * Esta clase representa la relación uno a muchos entre un pedido y sus gráficos asociados,
 * incluyendo para cada gráfico la lista de hilos (a través de GraficoConHilos).
 *
 * @property pedido La entidad PedidoEntity embebida que representa el pedido principal.
 * @property graficos Lista de GraficoConHilos relacionados con este pedido.
 *
 * @author Olga y Sandra Macías Aragón
 */
data class PedidoConGraficos(
    @Embedded
    val pedido: PedidoEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "idPedido",    /* campo en GraficoEntity que apunta a PedidoEntity.id */
        entity = GraficoEntity::class
    )
    val graficos: List<GraficoConHilos>
)
