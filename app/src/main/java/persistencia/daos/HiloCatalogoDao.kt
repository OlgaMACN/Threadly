package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.HiloCatalogoEntity

/*** @author Olga y Sandra Macías Aragón*/
@Dao
interface HiloCatalogoDao {

    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId ORDER BY numHilo ASC")
    fun obtenerHilosPorUsuario(userId: Int): List<HiloCatalogoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarHilos(hilos: List<HiloCatalogoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarHilo(hilo: HiloCatalogoEntity): Long

    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId AND numHilo = :numHilo LIMIT 1")
    fun obtenerHiloPorNumYUsuario(numHilo: String, userId: Int): HiloCatalogoEntity?

    @Update
    suspend fun actualizarHilo(hilo: HiloCatalogoEntity)

    @Delete
    suspend fun eliminarHilo(hilo: HiloCatalogoEntity)

    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId AND numHilo = :numHilo")
    fun eliminarPorNumYUsuario(numHilo: String, userId: Int)

    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId")
    suspend fun eliminarTodoPorUsuario(userId: Int)
}
