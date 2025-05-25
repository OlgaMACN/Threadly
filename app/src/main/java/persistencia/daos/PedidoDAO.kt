package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Pedido

/**
 * DAO para la entidad [Pedido].
 *
 * Proporciona métodos para insertar, actualizar, eliminar y consultar pedidos en la base de datos.
 */
@Dao
interface PedidoDAO {

    /**
     * Inserta un nuevo pedido en la base de datos.
     *
     * @param pedido Objeto [Pedido] a insertar.
     * @return El ID generado para el pedido insertado.
     */
    @Insert
    suspend fun insertar(pedido: Pedido): Long

    /**
     * Actualiza un pedido existente en la base de datos.
     *
     * @param pedido Objeto [Pedido] con los datos actualizados.
     */
    @Update
    suspend fun actualizar(pedido: Pedido)

    /**
     * Elimina un pedido de la base de datos.
     *
     * @param pedido Objeto [Pedido] a eliminar.
     */
    @Delete
    suspend fun eliminar(pedido: Pedido)

    /**
     * Obtiene la lista de pedidos asociados a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Lista de pedidos correspondientes al usuario.
     */
    @Query("SELECT * FROM Pedido WHERE userId = :userId")
    suspend fun obtenerPorUsuario(userId: Int): List<Pedido>

    /**
     * Obtiene un pedido por su ID.
     *
     * @param id ID del pedido.
     * @return Objeto [Pedido] correspondiente o null si no existe.
     */
    @Query("SELECT * FROM Pedido WHERE pedidoId = :id")
    suspend fun obtenerPorId(id: Int): Pedido?
}
