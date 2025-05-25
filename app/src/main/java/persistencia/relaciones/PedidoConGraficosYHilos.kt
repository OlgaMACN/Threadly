package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Grafico
import persistencia.entidades.Pedido

/**
 * Representa la relación entre un [Pedido], sus [Grafico] asociados
 * y los [GraficoHilo] relacionados con cada gráfico.
 *
 * Esta clase permite obtener un pedido junto con todos sus gráficos,
 * y a su vez, los hilos que corresponden a cada gráfico.
 *
 * @property pedido El objeto [Pedido] embebido.
 * @property graficosConHilos La lista de objetos [GraficoConHilos] relacionados,
 * que contienen cada gráfico junto con sus hilos asociados.
 */
data class PedidoConGraficosYHilos(
    @Embedded val pedido: Pedido,

    @Relation(
        entity = persistencia.entidades.Grafico::class,
        parentColumn = "pedidoId",
        entityColumn = "pedidoId"
    )
    val graficosConHilos: List<GraficoConHilos>
)
