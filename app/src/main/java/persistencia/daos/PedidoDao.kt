package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import persistencia.entidades.PedidoConGraficos
import persistencia.entidades.PedidoEntity

@Dao
interface PedidoDao {

    /**
     * Inserta un pedido en la tabla "pedidos" y devuelve su id generado.
     */
    @Insert
    suspend fun insertarPedido(p: PedidoEntity): Long

    /**
     * Obtiene todos los pedidos guardados de un usuario dado, ordenados por nombre.
     */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE userId = :userId ORDER BY nombre")
    suspend fun obtenerPedidosConGraficos(userId: Int): List<PedidoConGraficos>

    /**
     * (Opcional) Elimina un pedido completo (si lo necesitas).
     */
    @Query("DELETE FROM pedidos WHERE id = :pedidoId")
    suspend fun eliminarPedido(pedidoId: Int)
}