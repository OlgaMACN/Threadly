package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import persistencia.entidades.GraficoHilo

@Dao
interface GraficoHiloDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vararg relacion: GraficoHilo)

    @Query("SELECT * FROM GraficoHilo WHERE graphicId = :graficoId")
    suspend fun obtenerHilosDeGrafico(graficoId: Int): List<GraficoHilo>

    @Query("DELETE FROM GraficoHilo WHERE graphicId = :graficoId")
    suspend fun eliminarDeGrafico(graficoId: Int)
}