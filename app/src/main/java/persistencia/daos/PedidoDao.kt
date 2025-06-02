package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import persistencia.entidades.PedidoEntity

@Dao
interface PedidoDao {
    /**
     * Inserta un nuevo pedido, devolviendo el ID generado.
     */
    @Insert
    suspend fun insertarPedido(pedido: PedidoEntity): Long

    /**
     * (Opcional) Si quieres listar todos los pedidos guardados para un usuario:
     */
    @Query("SELECT * FROM pedido_entity WHERE userId = :userId ORDER BY fecha DESC")
    suspend fun obtenerPedidosPorUsuario(userId: Int): List<PedidoEntity>
}