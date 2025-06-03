package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import persistencia.entidades.GraficoEntity

@Dao
interface GraficoDao {
    @Insert
    suspend fun insertarGrafico(g: GraficoEntity): Long

    /**
     * Obtiene todos los gráficos “en curso” (idPedido IS NULL) para el usuario dado.
     */
    @Query("SELECT * FROM graficos WHERE userId = :userId AND idPedido IS NULL ORDER BY nombre")
    suspend fun obtenerGraficosEnCurso(userId: Int): List<GraficoEntity>

    @Query("SELECT id FROM graficos WHERE nombre = :nombreGrafico LIMIT 1")
    suspend fun obtenerIdPorNombre(nombreGrafico: String): Int?

    /**
     * Asocia todos los gráficos EN CURSO (idPedido IS NULL) de este userId al pedido con nuevoId.
     */
    @Query("UPDATE graficos SET idPedido = :nuevoId WHERE userId = :userId AND idPedido IS NULL")
    suspend fun asociarGraficosAlPedido(userId: Int, nuevoId: Int)

    /**
     * Obtiene un gráfico por nombre y userId (solo en curso).
     * Para comprobar duplicados de nombre en curso.
     */
    @Query("SELECT * FROM graficos WHERE userId = :userId AND idPedido IS NULL AND nombre = :nombreGrafico LIMIT 1")
    suspend fun obtenerGraficoEnCursoPorNombre(userId: Int, nombreGrafico: String): GraficoEntity?

    /**
     * Opcional: si quieres borrar un gráfico “en curso” concreto (por ej. al eliminarlo manualmente).
     */
    @Query("DELETE FROM graficos WHERE id = :graficoId AND idPedido IS NULL")
    suspend fun eliminarGraficoEnCurso(graficoId: Int)

    @Query("SELECT * FROM graficos WHERE id = :id")
    suspend fun obtenerGraficoPorId(id: Int): GraficoEntity?

    @Query("SELECT * FROM graficos WHERE nombre = :nombre")
    suspend fun obtenerGraficoPorNombre(nombre: String): GraficoEntity?
}