package persistencia.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import persistencia.entidades.GraficoPedidoEntity
import persistencia.entidades.HiloGraficoEntity
import persistencia.entidades.PedidoEntity
import persistencia.relaciones.PedidoConGraficos

@Dao
interface PedidoDAO {

    @Insert
    suspend fun insertPedido(pedido: PedidoEntity): Long

    @Insert
    suspend fun insertGraficos(graficos: List<GraficoPedidoEntity>): List<Long>

    @Insert
    suspend fun insertHilos(hilos: List<HiloGraficoEntity>)

    @Transaction
    @Query("SELECT * FROM pedidos")
    suspend fun getPedidosConGraficos(): List<PedidoConGraficos>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun getPedidoConGraficos(pedidoId: Int): PedidoConGraficos?

    @Query("DELETE FROM graficos_pedido WHERE pedidoId = :pedidoId")
    suspend fun deleteGraficosDePedido(pedidoId: Int)

    @Query("DELETE FROM hilos_grafico WHERE graficoId = :graficoId")
    suspend fun deleteHilosDeGrafico(graficoId: Int)

    @Delete
    suspend fun deletePedido(pedido: PedidoEntity)

}
