package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.HiloStockEntity

/**
 * DAO para acceder y manipular el stock de hilos por usuario.
 *
 * Permite operaciones CRUD y consultas específicas para gestionar
 * la cantidad de madejas de cada hilo en el inventario de un usuario.
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @see HiloStockEntity
 *
 */
@Dao
interface HiloStockDao {
    /**
     * Inserta una lista de hilos en stock ignorando conflictos.
     *
     * @param hilos Lista de [HiloStockEntity] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodo(hilos: List<HiloStockEntity>)

    /**
     * Inserta o reemplaza un registro de stock.
     *
     * @param hilo [HiloStockEntity] a insertar o actualizar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(hilo: HiloStockEntity)

    /**
     * Inserta una lista de registros de stock. Ignora los que generen conflicto.
     *
     * @param list Lista de [HiloStockEntity] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarStocks(list: List<HiloStockEntity>)

    /**
     * Inserta un registro de stock. Si ya existe (conflicto), lo ignora.
     *
     * @param ent Entidad [HiloStockEntity] a insertar.
     * @return ID generado o -1 si fue ignorado por conflicto.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarStock(ent: HiloStockEntity): Long

    /**
     * Cuenta el número de registros de stock de hilos asociados a un usuario.
     *
     * @param userId ID del usuario.
     * @return Cantidad total de registros de stock para el usuario.
     */
    @Query("SELECT COUNT(*) FROM hilo_stock WHERE usuarioId = :userId")
    suspend fun contarStocksPorUsuario(userId: Int): Int

    /**
     * Obtiene un registro de stock específico de un hilo para un usuario dado.
     *
     * @param h Código o ID del hilo.
     * @param uid ID del usuario.
     * @return El [HiloStockEntity] si existe, o null si no.
     */
    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :uid AND hiloId = :h")
    suspend fun obtenerPorHiloUsuario(h: String, uid: Int): HiloStockEntity?

    /**
     * Obtiene todos los registros de stock de un usuario.
     *
     * @param userId ID del usuario.
     * @return Lista de [HiloStockEntity] del usuario.
     */
    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :userId")
    suspend fun obtenerStockPorUsuario(userId: Int): List<HiloStockEntity>

    /**
     * Obtiene la suma total de madejas para un hilo dado de un usuario.
     *
     * @param userId ID del usuario.
     * @param hiloId Código o ID del hilo.
     * @return Suma total de madejas, o null si no hay registros.
     */
    @Query("SELECT SUM(madejas) FROM hilo_stock WHERE usuarioId = :userId AND hiloId = :hiloId")
    suspend fun obtenerMadejas(userId: Int, hiloId: String): Int?

    /**
     * Elimina un registro de stock por usuario e hilo.
     *
     * @param usuarioId ID del usuario.
     * @param hiloId Código o ID del hilo.
     */
    @Query("DELETE FROM hilo_stock WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun eliminarPorUsuarioYHilo(usuarioId: Int, hiloId: String)

    /**
     * Actualiza la cantidad de madejas para un hilo específico de un usuario.
     *
     * @param usuarioId ID del usuario.
     * @param hiloId Código o ID del hilo.
     * @param nuevas Nueva cantidad de madejas.
     */
    @Query("UPDATE hilo_stock SET madejas = :nuevas WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun actualizarMadejas(usuarioId: Int, hiloId: String, nuevas: Int)

    /**
     * Elimina un registro de stock por usuario e hilo.
     *
     * @param usuarioId ID del usuario.
     * @param hiloId Código o ID del hilo.
     */
    @Query("DELETE FROM hilo_stock WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun eliminar(usuarioId: Int, hiloId: String)

    /**
     * Busca un registro de stock por usuario e hilo.
     *
     * @param usuarioId ID del usuario.
     * @param hiloId Código o ID del hilo.
     * @return El [HiloStockEntity] si existe, o null.
     */
    @Query("SELECT * FROM hilo_stock WHERE usuarioId = :usuarioId AND hiloId = :hiloId")
    suspend fun buscar(usuarioId: Int, hiloId: String): HiloStockEntity?

    /**
     * Actualiza un registro de stock específico.
     *
     * @param hilo [HiloStockEntity] con datos actualizados.
     */
    @Update
    suspend fun actualizarHiloStock(hilo: HiloStockEntity)

    /**
     * Actualiza un registro de stock.
     *
     * @param ent Entidad [HiloStockEntity] con datos actualizados.
     */
    @Update
    suspend fun actualizarStock(ent: HiloStockEntity)

    /**
     * Elimina un registro de stock.
     *
     * @param ent Entidad [HiloStockEntity] a eliminar.
     */
    @Delete
    suspend fun eliminarStock(ent: HiloStockEntity)
}
