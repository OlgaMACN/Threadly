package persistencia.dao

import androidx.room.*
import persistencia.entidades.HiloStockEnt

@Dao
interface HiloStockDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(vararg hilos: HiloStockEnt)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarHilo(hilo: HiloStockEnt)

    @Query("SELECT * FROM stock_hilos")
    suspend fun obtenerTodos(): List<HiloStockEnt>

    @Query("SELECT * FROM stock_hilos WHERE hiloId = :id")
    suspend fun buscarPorId(id: String): HiloStockEnt?

    @Update
    suspend fun actualizar(hilo: HiloStockEnt)

    @Update
    suspend fun actualizarHilo(hilo: HiloStockEnt)

    @Delete
    suspend fun eliminar(hilo: HiloStockEnt)

    @Delete
    suspend fun eliminarHilo(hilo: HiloStockEnt)
}
