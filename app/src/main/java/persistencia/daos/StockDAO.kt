package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Stock

/**
 * DAO para la entidad [Stock].
 *
 * Proporciona métodos para insertar, actualizar, eliminar y consultar el stock de hilos de los usuarios en la base de datos.
 */
@Dao
interface StockDAO {

    /**
     * Inserta un registro de stock.
     * Si ya existe un registro con la misma clave primaria, lo reemplaza.
     *
     * @param stock Objeto [Stock] a insertar o reemplazar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(stock: Stock)

    /**
     * Actualiza un registro de stock existente.
     *
     * @param stock Objeto [Stock] con los datos actualizados.
     */
    @Update
    suspend fun actualizar(stock: Stock)

    /**
     * Elimina el stock asociado a un usuario específico.
     *
     * @param userId ID del usuario cuyo stock se eliminará.
     */
    @Query("DELETE FROM Stock WHERE userId = :userId")
    suspend fun eliminarTodoDeUsuario(userId: Int)

    /**
     * Obtiene la lista de stock de hilos para un usuario.
     *
     * @param userId ID del usuario.
     * @return Lista de objetos [Stock] correspondientes al usuario.
     */
    @Query("SELECT * FROM Stock WHERE userId = :userId")
    suspend fun obtenerPorUsuario(userId: Int): List<Stock>

    /**
     * Obtiene un hilo específico en el stock de un usuario.
     *
     * @param userId ID del usuario.
     * @param threadId ID del hilo (threadId).
     * @return Objeto [Stock] correspondiente o null si no existe.
     */
    @Query("SELECT * FROM Stock WHERE userId = :userId AND threadId = :threadId")
    suspend fun obtenerHiloStock(userId: Int, threadId: String): Stock?
}
