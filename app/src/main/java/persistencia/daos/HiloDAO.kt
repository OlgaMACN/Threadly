package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import persistencia.entidades.Hilo

@Dao
interface HiloDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHilos(hilos: List<Hilo>)

    @Query("SELECT * FROM Hilo ORDER BY codigo ASC")
    suspend fun obtenerTodos(): List<Hilo>

    @Query("SELECT * FROM Hilo WHERE threadId = :id")
    suspend fun obtenerPorId(id: String): Hilo?
}