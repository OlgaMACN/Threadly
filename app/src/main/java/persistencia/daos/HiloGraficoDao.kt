package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.GraficoEntity
import persistencia.entidades.HiloGraficoEntity

@Dao
interface HiloGraficoDao {
    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId")
    suspend fun obtenerHilosDeGrafico(graficoId: Int): List<HiloGraficoEntity>

    @Query("SELECT id FROM graficos WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerIdPorNombre(nombre: String): Int?

    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId AND hilo = :hilo LIMIT 1")
    suspend fun obtenerPorGraficoYHilo(graficoId: Int, hilo: String): HiloGraficoEntity?

    @Insert
    suspend fun insertarGrafico(grafico: GraficoEntity): Long

    @Query("UPDATE hilos_grafico SET madejas = :nuevaCantidad WHERE hilo = :codigoHilo")
    suspend fun actualizarMadejas(codigoHilo: String, nuevaCantidad: Int)

    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId")
    fun obtenerHilosPorGrafico(graficoId: Int): List<HiloGraficoEntity>

    @Update
    suspend fun actualizarHilo(hilo: HiloGraficoEntity)

    @Query("DELETE FROM hilos_grafico WHERE graficoId = :graficoId AND hilo = :nombreHilo")
    fun eliminarHiloDeGrafico(graficoId: Int, nombreHilo: String)

    @Query("SELECT madejas FROM hilos_grafico WHERE hilo = :hilo AND graficoId = :graficoId")
    suspend fun obtenerMadejas(hilo: String, graficoId: Int): Int?

    @Insert
    suspend fun insertarHiloEnGrafico(hilo: HiloGraficoEntity)

    @Delete
    suspend fun eliminarHiloDeGrafico(hilo: HiloGraficoEntity)
}