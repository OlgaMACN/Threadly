package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import persistencia.entidades.GraficoHilo

/**
 * Interfaz DAO para la entidad intermedia [GraficoHilo].
 *
 * Proporciona las operaciones para gestionar la relación entre gráficos y sus hilos,
 * incluyendo inserción, consulta y eliminación.
 */
@Dao
interface GraficoHiloDAO {

    /**
     * Inserta una o varias relaciones [GraficoHilo] en la base de datos.
     * Si ya existe una relación con la misma clave primaria, la reemplaza.
     *
     * @param relacion Vararg de objetos [GraficoHilo] a insertar o reemplazar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vararg relacion: GraficoHilo)

    /**
     * Obtiene la lista de hilos asociados a un gráfico específico.
     *
     * @param graficoId El ID del gráfico cuyos hilos se desean obtener.
     * @return Lista de objetos [GraficoHilo] asociados al gráfico.
     */
    @Query("SELECT * FROM GraficoHilo WHERE graphicId = :graficoId")
    suspend fun obtenerHilosDeGrafico(graficoId: Int): List<GraficoHilo>

    /**
     * Elimina todas las relaciones entre hilos y un gráfico dado.
     *
     * @param graficoId El ID del gráfico cuyas relaciones se eliminarán.
     */
    @Query("DELETE FROM GraficoHilo WHERE graphicId = :graficoId")
    suspend fun eliminarDeGrafico(graficoId: Int)
}
