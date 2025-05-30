package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.HiloStockEntity

@Dao
interface HiloStockDao {

    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :uid")
    suspend fun obtenerStockPorUsuario(uid: Int): List<HiloStockEntity>

    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :uid AND hiloId = :h")
    suspend fun obtenerPorHiloUsuario(h: String, uid: Int): HiloStockEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarStock(ent: HiloStockEntity): Long

    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :userId")
    suspend fun obtenerPorUsuario(userId: Int): List<HiloStockEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarStocks(list: List<HiloStockEntity>)

    @Query("DELETE FROM hilo_stock WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun eliminarPorUsuarioYHilo(usuarioId: Int, hiloId: String)

    @Update
    suspend fun actualizarStock(ent: HiloStockEntity)

    @Delete
    suspend fun eliminarStock(ent: HiloStockEntity)

    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :usuarioId ORDER BY hiloId ASC")
    suspend fun obtenerTodo(usuarioId: Int): List<HiloStockEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodo(hilos: List<HiloStockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(hilo: HiloStockEntity)

    @Query("UPDATE hilo_stock SET madejas = :nuevas WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun actualizarMadejas(usuarioId: Int, hiloId: String, nuevas: Int)

    @Query("DELETE FROM hilo_stock WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun eliminar(usuarioId: Int, hiloId: String)

    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun buscar(usuarioId: Int, hiloId: String): HiloStockEntity?

    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :usuarioId")
    suspend fun obtenerStockDeUsuario(usuarioId: Int): List<HiloStockEntity>

    @Update
    suspend fun actualizarHiloStock(hilo: HiloStockEntity)
}