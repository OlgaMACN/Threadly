package persistencia.daos

import androidx.room.*
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

    @Update
    suspend fun actualizarHilo(hilo: HiloCatalogoEntity)

    @Delete
    suspend fun eliminarHilo(hilo: HiloCatalogoEntity)

    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId")
    suspend fun eliminarTodoPorUsuario(userId: Int)
}
