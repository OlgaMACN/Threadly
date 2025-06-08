package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import persistencia.entidades.PedidoConGraficos
import persistencia.entidades.PedidoEntity

/**
 * DAO para acceder y manipular la tabla de pedidos.
 *
 * Permite realizar operaciones CRUD y consultas específicas
 * para gestionar pedidos y obtener sus relaciones con gráficos.
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @see PedidoEntity
 * @see PedidoConGraficos
 */
@Dao
interface PedidoDao {
    /**
     * Inserta un pedido o reemplaza si ya existe conflicto por ID.
     *
     * @param pedido Pedido a insertar o reemplazar.
     * @return ID generado o actualizado del pedido.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity): Long

    /**
     * Inserta un pedido en la base de datos.
     *
     * @param p Pedido a insertar.
     * @return ID generado del pedido insertado.
     */
    @Insert
    suspend fun insertarPedido(p: PedidoEntity): Long

    /**
     * Obtiene todos los pedidos de un usuario, ordenados por nombre.
     *
     * @param userId ID del usuario.
     * @return Lista de pedidos del usuario.
     */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE userId = :userId ORDER BY nombre")
    suspend fun obtenerTodosPorUsuario(userId: Int): List<PedidoEntity>

    /**
     * Obtiene un pedido con todos sus gráficos relacionados.
     *
     * @param pedidoId ID del pedido.
     * @return Pedido junto con sus gráficos.
     */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun obtenerPedidoConGraficos(pedidoId: Int): PedidoConGraficos

    /**
     * Obtiene el último pedido creado por un usuario.
     *
     * @param userId ID del usuario.
     * @return Último pedido o null si no hay ninguno.
     */
    @Query("SELECT * FROM pedidos WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun obtenerUltimoPedido(userId: Int): PedidoEntity?

    /**
     * Elimina un pedido por su ID (opcional).
     *
     * @param pedidoId ID del pedido a eliminar.
     */
    @Query("DELETE FROM pedidos WHERE id = :pedidoId")
    suspend fun eliminarPedido(pedidoId: Int)

    /**
     * Actualiza un pedido existente.
     *
     * @param pedido Pedido con datos actualizados.
     */
    @Update
    suspend fun actualizar(pedido: PedidoEntity)

    /**
     * Actualiza un pedido (alias para [actualizar]).
     *
     * @param pedido Pedido con datos actualizados.
     */
    @Update
    suspend fun updatePedido(pedido: PedidoEntity)

    /**
     * Elimina un pedido específico.
     *
     * @param pedido Pedido a eliminar.
     */
    @Delete
    suspend fun eliminar(pedido: PedidoEntity)

    /**
     * Elimina un pedido (alias para [eliminar]).
     *
     * @param pedido Pedido a eliminar.
     */
    @Delete
    suspend fun deletePedido(pedido: PedidoEntity)
}
