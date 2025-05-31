package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import persistencia.entidades.GraficoEntity

@Dao
interface GraficoDao {
    @Insert
    suspend fun insertarGrafico(g: GraficoEntity): Long


    @Query("SELECT id FROM graficos WHERE nombre = :nombreGrafico LIMIT 1")
    suspend fun obtenerIdPorNombre(nombreGrafico: String): Int?

    @Query("SELECT * FROM graficos WHERE id = :id")
    suspend fun obtenerGraficoPorId(id: Int): GraficoEntity?

    @Query("SELECT * FROM graficos WHERE nombre = :nombre")
    suspend fun obtenerGraficoPorNombre(nombre: String): GraficoEntity?
}