package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import logica.pedido_hilos.Grafico

/**
 * Interfaz DAO para la entidad [Grafico].
 *
 * Define las operaciones de acceso a la base de datos relacionadas con los gráficos,
 * incluyendo inserción, actualización, eliminación y consultas específicas.
 */
@Dao
interface GraficoDAO {

    /**
     * Inserta un nuevo gráfico en la base de datos.
     *
     * @param grafico El objeto [Grafico] a insertar.
     * @return El ID generado para el gráfico insertado.
     */
    @Insert
    suspend fun insertar(grafico: Grafico): Long

    /**
     * Actualiza un gráfico existente en la base de datos.
     *
     * @param grafico El objeto [Grafico] con los datos actualizados.
     */
    @Update
    suspend fun actualizar(grafico: Grafico)

    /**
     * Elimina un gráfico de la base de datos.
     *
     * @param grafico El objeto [Grafico] a eliminar.
     */
    @Delete
    suspend fun eliminar(grafico: Grafico)

    /**
     * Obtiene todos los gráficos asociados a un pedido específico.
     *
     * @param pedidoId El ID del pedido cuyos gráficos se desean obtener.
     * @return Lista de gráficos que pertenecen al pedido especificado.
     */
    @Query("SELECT * FROM Grafico WHERE pedidoId = :pedidoId")
    suspend fun obtenerPorPedido(pedidoId: Int): List<Grafico>

    /**
     * Obtiene un gráfico por su ID.
     *
     * @param id El ID del gráfico.
     * @return El gráfico correspondiente, o null si no existe.
     */
    @Query("SELECT * FROM Grafico WHERE graphicId = :id")
    suspend fun obtenerPorId(id: Int): Grafico?
}
