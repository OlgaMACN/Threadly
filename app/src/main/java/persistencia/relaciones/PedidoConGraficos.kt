package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Grafico
import persistencia.entidades.Pedido

/**
 * Representa la relación entre un [Pedido] y sus [Grafico] asociados.
 *
 * Esta clase permite obtener un pedido junto con la lista de gráficos vinculados a dicho pedido
 * mediante la anotación [Relation] de Room.
 *
 * @property pedido El objeto [Pedido] embebido.
 * @property graficos La lista de objetos [Grafico] relacionados con el pedido.
 */
data class PedidoConGraficos(
    @Embedded val pedido: Pedido,

    @Relation(
        parentColumn = "pedidoId",
        entityColumn = "pedidoId"
    )
    val graficos: List<Grafico>
)
