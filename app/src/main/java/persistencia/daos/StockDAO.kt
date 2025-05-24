package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Stock

@Dao
interface StockDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(stock: Stock)

    @Update
    suspend fun actualizar(stock: Stock)

    @Query("DELETE FROM Stock WHERE userId = :userId")
    suspend fun eliminarTodoDeUsuario(userId: Int)

    @Query("SELECT * FROM Stock WHERE userId = :userId")
    suspend fun obtenerPorUsuario(userId: Int): List<Stock>

    @Query("SELECT * FROM Stock WHERE userId = :userId AND threadId = :threadId")
    suspend fun obtenerHiloStock(userId: Int, threadId: String): Stock?
}