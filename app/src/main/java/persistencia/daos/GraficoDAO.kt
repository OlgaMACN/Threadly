package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.GraficoEntidad


/**
 * Interfaz DAO para la entidad [GraficoEntidad].
 *
 * Define las operaciones de acceso a la base de datos relacionadas con los gráficos,
 * incluyendo inserción, actualización, eliminación y consultas específicas.
 */
@Dao
interface GraficoDAO {

    /**
     * Inserta un nuevo gráfico en la base de datos.
     *
     * @param grafico El objeto [GraficoEntidad] a insertar.
     * @return El ID generado para el gráfico insertado.
     */
    @Insert
    suspend fun insertar(grafico: GraficoEntidad): Long

    /**
     * Actualiza un gráfico existente en la base de datos.
     *
     * @param grafico El objeto [GraficoEntidad] con los datos actualizados.
     */
    @Update
    suspend fun actualizar(grafico: GraficoEntidad)

    /**
     * Elimina un gráfico de la base de datos.
     *
     * @param grafico El objeto [GraficoEntidad] a eliminar.
     */
    @Delete
    suspend fun eliminar(grafico: GraficoEntidad)

    /**
     * Obtiene todos los gráficos asociados a un pedido específico.
     *
     * @param pedidoId El ID del pedido cuyos gráficos se desean obtener.
     * @return Lista de gráficos que pertenecen al pedido especificado.
     */
    @Query("SELECT * FROM GraficoEntidad WHERE pedidoId = :pedidoId")
    suspend fun obtenerPorPedido(pedidoId: Int): List<GraficoEntidad>

    /**
     * Obtiene un gráfico por su ID.
     *
     * @param id El ID del gráfico.
     * @return El gráfico correspondiente, o null si no existe.
     */
    @Query("SELECT * FROM GraficoEntidad WHERE graphicId = :id")
    suspend fun obtenerPorId(id: Int): GraficoEntidad?
}
