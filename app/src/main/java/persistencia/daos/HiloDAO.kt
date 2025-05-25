package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import persistencia.entidades.Hilo

/**
 * DAO para la entidad [Hilo].
 *
 * Proporciona métodos para insertar, consultar y obtener hilos individuales.
 */
@Dao
interface HiloDAO {

    /**
     * Inserta una lista de hilos en la base de datos.
     * En caso de conflicto, reemplaza las filas existentes.
     *
     * @param hilos Lista de objetos [Hilo] a insertar o reemplazar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHilos(hilos: List<Hilo>)

    /**
     * Obtiene todos los hilos ordenados alfabéticamente por el código.
     *
     * @return Lista con todos los hilos ordenados por el campo [Hilo.codigo].
     */
    @Query("SELECT * FROM Hilo ORDER BY codigo ASC")
    suspend fun obtenerTodos(): List<Hilo>

    /**
     * Obtiene un hilo por su ID único (threadId).
     *
     * @param id Identificador único del hilo.
     * @return Objeto [Hilo] correspondiente o null si no existe.
     */
    @Query("SELECT * FROM Hilo WHERE threadId = :id")
    suspend fun obtenerPorId(id: String): Hilo?
}
