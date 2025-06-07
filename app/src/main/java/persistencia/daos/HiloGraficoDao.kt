package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.GraficoEntity
import persistencia.entidades.HiloGraficoEntity

/**
 * DAO para acceder y modificar las tablas relacionadas con hilos dentro de gráficos
 * y los propios gráficos.
 *
 * Proporciona métodos para obtener, insertar, actualizar y eliminar hilos en gráficos,
 * así como consultar y modificar propiedades específicas de los gráficos.
 *
 * Todas las operaciones son suspend functions salvo las que retornan valores simples o listas.
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @see HiloGraficoEntity
 *
 */
@Dao
interface HiloGraficoDao {
    /**
     * Inserta un nuevo gráfico en la base de datos.
     *
     * @param grafico El [GraficoEntity] a insertar.
     * @return El ID generado para el nuevo gráfico.
     */
    @Insert
    suspend fun insertarGrafico(grafico: GraficoEntity): Long

    /**
     * Inserta un hilo en la tabla de hilos de gráfico.
     *
     * @param hilo El [HiloGraficoEntity] a insertar.
     */
    @Insert
    suspend fun insertarHiloEnGrafico(hilo: HiloGraficoEntity)

    /**
     * Obtiene la lista de hilos que pertenecen a un gráfico dado.
     *
     * @param graficoId ID del gráfico.
     * @return Lista de [HiloGraficoEntity] asociados al gráfico.
     */
    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId")
    suspend fun obtenerHilosDeGrafico(graficoId: Int): List<HiloGraficoEntity>

    /**
     * Obtiene el valor de "count" (cantidad de tela) almacenado para un gráfico.
     *
     * @param graficoId ID del gráfico.
     * @return Cantidad de tela (count) o null si no existe.
     */
    @Query("SELECT count FROM graficos WHERE id = :graficoId")
    fun obtenerCountTela(graficoId: Int): Int?

    /**
     * Actualiza el valor de "count" (cantidad de tela) de un gráfico.
     *
     * @param graficoId ID del gráfico.
     * @param nuevoCount Nuevo valor de cantidad de tela (puede ser null).
     */
    @Query("UPDATE graficos SET count = :nuevoCount WHERE id = :graficoId")
    fun actualizarCountTela(graficoId: Int, nuevoCount: Int?)

    /**
     * Obtiene el ID de un gráfico a partir de su nombre.
     *
     * @param nombre Nombre del gráfico.
     * @return ID del gráfico si existe, o null si no se encuentra.
     */
    @Query("SELECT id FROM graficos WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerIdPorNombre(nombre: String): Int?

    /**
     * Cuenta el número de hilos asociados a un gráfico específico.
     *
     * @param graficoId ID del gráfico.
     * @return Cantidad de hilos.
     */
    @Query("SELECT COUNT(*) FROM hilos_grafico WHERE graficoId = :graficoId")
    suspend fun countHilosDeGrafico(graficoId: Int): Int

    /**
     * Obtiene un hilo específico de un gráfico por su nombre de hilo.
     *
     * @param graficoId ID del gráfico.
     * @param hilo Nombre del hilo.
     * @return El [HiloGraficoEntity] si existe, o null si no.
     */
    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId AND hilo = :hilo LIMIT 1")
    suspend fun obtenerPorGraficoYHilo(graficoId: Int, hilo: String): HiloGraficoEntity?

    /**
     * Actualiza la cantidad de madejas de un hilo identificado por su código.
     *
     * @param codigoHilo Código/nombre del hilo.
     * @param nuevaCantidad Nueva cantidad de madejas.
     */
    @Query("UPDATE hilos_grafico SET madejas = :nuevaCantidad WHERE hilo = :codigoHilo")
    suspend fun actualizarMadejas(codigoHilo: String, nuevaCantidad: Int)

    /**
     * Obtiene la lista de hilos asociados a un gráfico.
     *
     * NOTA: Este método NO es suspend.
     *
     * @param graficoId ID del gráfico.
     * @return Lista de [HiloGraficoEntity] asociados.
     */
    @Query("SELECT * FROM hilos_grafico WHERE graficoId = :graficoId")
    fun obtenerHilosPorGrafico(graficoId: Int): List<HiloGraficoEntity>

    /**
     * Elimina un hilo específico de un gráfico dado por nombre de hilo.
     *
     * @param graficoId ID del gráfico.
     * @param nombreHilo Nombre del hilo a eliminar.
     */
    @Query("DELETE FROM hilos_grafico WHERE graficoId = :graficoId AND hilo = :nombreHilo")
    fun eliminarHiloDeGrafico(graficoId: Int, nombreHilo: String)

    /**
     * Obtiene la cantidad de madejas de un hilo en un gráfico específico.
     *
     * @param hilo Nombre del hilo.
     * @param graficoId ID del gráfico.
     * @return Cantidad de madejas o null si no existe.
     */
    @Query("SELECT madejas FROM hilos_grafico WHERE hilo = :hilo AND graficoId = :graficoId")
    suspend fun obtenerMadejas(hilo: String, graficoId: Int): Int?

    /**
     * Actualiza los datos de un hilo dentro de un gráfico.
     *
     * @param hilo El [HiloGraficoEntity] con datos actualizados.
     */
    @Update
    suspend fun actualizarHilo(hilo: HiloGraficoEntity)

    /**
     * Elimina un hilo de un gráfico dado.
     *
     * @param hilo El [HiloGraficoEntity] a eliminar.
     */
    @Delete
    suspend fun eliminarHiloDeGrafico(hilo: HiloGraficoEntity)
}
