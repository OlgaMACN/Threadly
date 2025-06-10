package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.HiloCatalogoEntity
import persistencia.entidades.HiloGraficoEntity

/**
 * DAO para acceder a las tablas relacionadas con el catálogo de hilos
 * y los hilos asociados a gráficos.
 *
 * Proporciona métodos para contar, insertar, actualizar, eliminar y consultar
 * hilos en el catálogo de un usuario, así como gestionar los hilos dentro de gráficos.
 *
 * Todas las operaciones son suspend functions para usarse con corrutinas de Kotlin,
 * salvo los que retornan listas simples para consultas que no requieren suspensión.
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @see HiloCatalogoEntity
 * @see HiloGraficoEntity
 */
@Dao
interface HiloCatalogoDao {
    /**
     * Inserta una lista de hilos en el catálogo, ignorando aquellos que ya existan (conflicto IGNORE).
     *
     * @param hilos Lista de [HiloCatalogoEntity] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarHilos(hilos: List<HiloCatalogoEntity>)

    /**
     * Inserta un hilo en el catálogo, ignorando si ya existe (conflicto IGNORE).
     *
     * @param hilo El [HiloCatalogoEntity] a insertar.
     * @return El ID generado para el hilo insertado, o -1 si fue ignorado.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarHilo(hilo: HiloCatalogoEntity): Long

    /**
     * Inserta un hilo en la tabla de hilos de un gráfico.
     *
     * @param e El [HiloGraficoEntity] a insertar.
     */
    @Insert
    suspend fun insertarHiloEnGrafico(e: HiloGraficoEntity)

    /**
     * Cuenta la cantidad de hilos que tiene un usuario en su catálogo.
     *
     * @param userId ID del usuario.
     * @return Número total de hilos para ese usuario.
     */
    @Query("SELECT COUNT(*) FROM hilo_catalogo WHERE userId = :userId")
    suspend fun contarHilosPorUsuario(userId: Int): Int

    /**
     * Obtiene la lista de hilos ordenados por número para un usuario dado.
     *
     * NOTA: Este método NO es suspend porque retorna una lista simple para uso directo.
     *
     * @param userId ID del usuario.
     * @return Lista de [HiloCatalogoEntity] ordenada por numHilo ascendente.
     */
    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId ORDER BY numHilo ASC")
    fun obtenerHilosPorUsuario(userId: Int): List<HiloCatalogoEntity>

    /**
     * Cuenta la cantidad total de hilos para un usuario.
     *
     * @param usuarioId ID del usuario.
     * @return Número total de hilos.
     */
    @Query("SELECT COUNT(*) FROM hilo_catalogo WHERE userId = :usuarioId")
    suspend fun contarHilos(usuarioId: Int): Int

    /**
     * Obtiene un hilo específico por su número y usuario.
     *
     * @param numHilo Número identificador del hilo.
     * @param userId ID del usuario.
     * @return El hilo si existe, o null si no.
     */
    @Query("SELECT * FROM hilo_catalogo WHERE userId = :userId AND numHilo = :numHilo LIMIT 1")
    fun obtenerHiloPorNumYUsuario(numHilo: String, userId: Int): HiloCatalogoEntity?

    /**
     * Elimina un hilo específico de un gráfico dado.
     *
     * @param graficoId ID del gráfico.
     * @param hilo Número del hilo a eliminar.
     */
    @Query("DELETE FROM hilos_grafico WHERE graficoId = :graficoId AND hilo = :hilo")
    suspend fun eliminarHiloDeGrafico(graficoId: Int, hilo: String)

    /**
     * Verifica si un hilo con un código dado existe para un usuario.
     *
     * @param userId ID del usuario.
     * @param codigo Número identificador del hilo.
     * @return Número de coincidencias (0 = no existe, >0 existe).
     */
    @Query("SELECT COUNT(*) FROM hilo_catalogo WHERE userId = :userId AND numHilo = :codigo")
    suspend fun existeHilo(userId: Int, codigo: String): Int

    /**
     * Elimina un hilo específico de un usuario dado, por número de hilo.
     *
     * @param numHilo Número del hilo.
     * @param userId ID del usuario.
     */
    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId AND numHilo = :numHilo")
    fun eliminarPorNumYUsuario(numHilo: String, userId: Int)

    /**
     * Elimina todos los hilos del catálogo para un usuario dado.
     *
     * @param userId ID del usuario.
     */
    @Query("DELETE FROM hilo_catalogo WHERE userId = :userId")
    suspend fun eliminarTodoPorUsuario(userId: Int)

    /**
     * Actualiza un hilo existente en el catálogo.
     *
     * @param hilo El objeto [HiloCatalogoEntity] con los datos actualizados.
     */
    @Update
    suspend fun actualizarHilo(hilo: HiloCatalogoEntity)

    /**
     * Elimina un hilo del catálogo.
     *
     * @param hilo El objeto [HiloCatalogoEntity] a eliminar.
     */
    @Delete
    suspend fun eliminarHilo(hilo: HiloCatalogoEntity)
}
