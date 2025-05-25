package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.GraficoEntidad
import persistencia.entidades.Pedido

/**
 * Representa la relación entre un [Pedido] y sus [GraficoEntidad] asociados.
 *
 * Esta clase permite obtener un pedido junto con la lista de gráficos vinculados a dicho pedido
 * mediante la anotación [Relation] de Room.
 *
 * @property pedido El objeto [Pedido] embebido.
 * @property graficos La lista de objetos [GraficoEntidad] relacionados con el pedido.
 */
data class PedidoConGraficos(
    @Embedded val pedido: Pedido,

    @Relation(
        parentColumn = "pedidoId",
        entityColumn = "pedidoId"
    )
    val graficos: List<GraficoEntidad>
)
