package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.HiloCatalogoEntity
import persistencia.entidades.HiloGraficoEntity

/*** @author Olga y Sandra Macías Aragón*/
@Dao
interface HiloCatalogoDao {
    @Query("SELECT COUNT(*) FROM hilo_catalogo WHERE userId = :userId")
    suspend fun contarHilosPorUsuario(userId: Int): Int


    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId ORDER BY numHilo ASC")
    fun obtenerHilosPorUsuario(userId: Int): List<HiloCatalogoEntity>

    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId")
    suspend fun obtenerTodosPorUsuario(userId: Int): List<HiloCatalogoEntity>

    @Query("SELECT COUNT(*) FROM hilo_catalogo WHERE userId = :usuarioId")
    suspend fun contarHilos(usuarioId: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarHilos(hilos: List<HiloCatalogoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarHilo(hilo: HiloCatalogoEntity): Long

    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId AND numHilo = :numHilo LIMIT 1")
    fun obtenerHiloPorNumYUsuario(numHilo: String, userId: Int): HiloCatalogoEntity?

    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId")
    suspend fun obtenerHilosPorGrafico(graficoId: Int): List<HiloGraficoEntity>

    @Insert
    suspend fun insertarHiloEnGrafico(e: HiloGraficoEntity)

    @Query("DELETE FROM hilos_grafico WHERE graficoId = :graficoId AND hilo = :hilo")
    suspend fun eliminarHiloDeGrafico(graficoId: Int, hilo: String)

    @Update
    suspend fun actualizarHilo(hilo: HiloCatalogoEntity)

    @Delete
    suspend fun eliminarHilo(hilo: HiloCatalogoEntity)

    @Query("SELECT COUNT(*) FROM hilo_catalogo WHERE userId = :userId AND numHilo = :codigo")
    suspend fun existeHilo(userId: Int, codigo: String): Int

    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId AND numHilo = :numHilo")
    fun eliminarPorNumYUsuario(numHilo: String, userId: Int)

    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId")
    suspend fun eliminarTodoPorUsuario(userId: Int)
}
