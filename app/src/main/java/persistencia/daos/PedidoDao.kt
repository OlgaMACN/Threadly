package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import persistencia.entidades.GraficoEntity
import persistencia.entidades.PedidoConGraficos
import persistencia.entidades.PedidoEntity

@Dao
interface PedidoDao {


    /*
     * Inserta un pedido en la tabla "pedidos" y devuelve su id generado.
     */
    @Insert
    suspend fun insertarPedido(p: PedidoEntity): Long

    @Transaction
    @Query("SELECT * FROM pedidos WHERE userId = :userId ORDER BY nombre")
    suspend fun obtenerTodosPorUsuario(userId: Int): List<PedidoEntity>

    @Query("SELECT * FROM graficos WHERE idPedido = :pedidoId AND userId = :userId")
    suspend fun obtenerGraficoPorPedido(userId: Int, pedidoId: Int): List<GraficoEntity>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun obtenerPedidoConGraficos(pedidoId: Int): PedidoConGraficos

    /**
     * Obtiene todos los pedidos guardados de un usuario dado, ordenados por nombre.
     */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE userId = :userId ORDER BY nombre")
    suspend fun obtenerPedidosConGraficos(userId: Int): List<PedidoConGraficos>

    @Query("SELECT * FROM pedidos WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun obtenerUltimoPedido(userId: Int): PedidoEntity?

    @Query("SELECT * FROM pedidos ORDER BY nombre")
    suspend fun obtenerTodos(): List<PedidoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity): Long

    @Update
    suspend fun actualizar(pedido: PedidoEntity)

    @Delete
    suspend fun eliminar(pedido: PedidoEntity)

    @Transaction
    @Query("SELECT * FROM pedidos WHERE userId = :userId")
    suspend fun getPedidosDelUsuario(userId: Int): List<PedidoConGraficos>

    @Delete
    suspend fun deletePedido(pedido: PedidoEntity)

    @Update
    suspend fun updatePedido(pedido: PedidoEntity)

    /**
     * (Opcional) Elimina un pedido completo (si lo necesitas).
     */
    @Query("DELETE FROM pedidos WHERE id = :pedidoId")
    suspend fun eliminarPedido(pedidoId: Int)
}