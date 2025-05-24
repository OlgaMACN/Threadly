package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Pedido

@Dao
interface PedidoDAO {
    @Insert
    suspend fun insertar(pedido: Pedido): Long

    @Update
    suspend fun actualizar(pedido: Pedido)

    @Delete
    suspend fun eliminar(pedido: Pedido)

    @Query("SELECT * FROM Pedido WHERE userId = :userId")
    suspend fun obtenerPorUsuario(userId: Int): List<Pedido>

    @Query("SELECT * FROM Pedido WHERE pedidoId = :id")
    suspend fun obtenerPorId(id: Int): Pedido?
}