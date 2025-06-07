package persistencia.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import persistencia.entidades.GraficoEntity

/**
 * DAO para acceder a la tabla de gráficos en la base de datos.
 *
 * Proporciona métodos para insertar, consultar, actualizar y eliminar objetos
 * de tipo [GraficoEntity]. Incluye operaciones para gestionar gráficos "en curso"
 * (es decir, aquellos que no están asociados a un pedido) y para asociar gráficos
 * a pedidos específicos.
 *
 * Todas las operaciones son suspend functions para usarse con corrutinas de Kotlin.
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @see GraficoEntity
 */
@Dao
interface GraficoDao {
    /**
     * Inserta un nuevo gráfico en la base de datos.
     *
     * @param g El objeto [GraficoEntity] a insertar.
     * @return El ID generado para el nuevo gráfico insertado.
     */
    @Insert
    suspend fun insertarGrafico(g: GraficoEntity): Long

    /**
     * Obtiene todos los gráficos “en curso” (idPedido IS NULL) para el usuario dado.
     *
     * @param userId ID del usuario.
     * @return Lista de [GraficoEntity] que están en curso para el usuario.
     */
    @Query("SELECT * FROM graficos WHERE userId = :userId AND idPedido IS NULL ORDER BY nombre")
    suspend fun obtenerGraficosEnCurso(userId: Int): List<GraficoEntity>

    /**
     * Obtiene el ID de un gráfico según su nombre.
     *
     * @param nombreGrafico Nombre del gráfico a buscar.
     * @return El ID del gráfico si existe, o null si no se encuentra.
     */
    @Query("SELECT id FROM graficos WHERE nombre = :nombreGrafico LIMIT 1")
    suspend fun obtenerIdPorNombre(nombreGrafico: String): Int?

    /**
     * Devuelve todos los gráficos que pertenezcan a un usuario y a un pedido dado.
     *
     * @param userId ID del usuario.
     * @param pedidoId ID del pedido.
     * @return Lista de gráficos asociados a ese pedido y usuario.
     */
    @Query("SELECT * FROM graficos WHERE userId = :userId AND idPedido = :pedidoId")
    suspend fun obtenerGraficoPorPedido(userId: Int, pedidoId: Int): List<GraficoEntity>

    /**
     * Asocia todos los gráficos en curso (idPedido IS NULL) de un usuario
     * al pedido con el ID especificado.
     *
     * @param userId ID del usuario.
     * @param nuevoId ID del pedido al que se asociarán los gráficos.
     */
    @Query("UPDATE graficos SET idPedido = :nuevoId WHERE userId = :userId AND idPedido IS NULL")
    suspend fun asociarGraficosAlPedido(userId: Int, nuevoId: Int)

    /**
     * Obtiene un gráfico en curso por nombre y usuario, útil para comprobar
     * duplicados de nombre en curso.
     *
     * @param userId ID del usuario.
     * @param nombreGrafico Nombre del gráfico.
     * @return El gráfico si existe, o null si no.
     */
    @Query("SELECT * FROM graficos WHERE userId = :userId AND idPedido IS NULL AND nombre = :nombreGrafico LIMIT 1")
    suspend fun obtenerGraficoEnCursoPorNombre(userId: Int, nombreGrafico: String): GraficoEntity?

    /**
     * Obtiene el último gráfico en curso creado por un usuario, ordenado por ID descendente.
     *
     * @param userId ID del usuario.
     * @return El último gráfico en curso, o null si no existe ninguno.
     */
    @Query(
        """
    SELECT * FROM graficos 
    WHERE userId = :userId AND idPedido IS NULL 
    ORDER BY id DESC 
    LIMIT 1
"""
    )
    suspend fun obtenerUltimoGraficoEnCurso(userId: Int): GraficoEntity?

    /**
     * Obtiene el último gráfico (de cualquier estado) creado por un usuario,
     * ordenado por ID descendente.
     *
     * @param userId ID del usuario.
     * @return El último gráfico creado, o null si no existe ninguno.
     */
    @Query("SELECT * FROM graficos WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun obtenerUltimoGrafico(userId: Int): GraficoEntity?

    /**
     * Elimina un gráfico en curso específico por su ID.
     *
     * @param graficoId ID del gráfico a eliminar.
     */
    @Query("DELETE FROM graficos WHERE id = :graficoId AND idPedido IS NULL")
    suspend fun eliminarGraficoEnCurso(graficoId: Int)

    /**
     * Obtiene un gráfico por su ID.
     *
     * @param id ID del gráfico.
     * @return El gráfico si existe, o null si no.
     */
    @Query("SELECT * FROM graficos WHERE id = :id")
    suspend fun obtenerGraficoPorId(id: Int): GraficoEntity?

    /**
     * Obtiene un gráfico por su nombre.
     *
     * @param nombre Nombre del gráfico.
     * @return El gráfico si existe, o null si no.
     */
    @Query("SELECT * FROM graficos WHERE nombre = :nombre")
    suspend fun obtenerGraficoPorNombre(nombre: String): GraficoEntity?
}
