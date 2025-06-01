package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import persistencia.entidades.PedidoEntity
import persistencia.relaciones.PedidoGraficoCrossRef
import persistencia.relaciones.PedidoWithGraficos
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    /**
     * Inserta un pedido nuevo y devuelve su ID generado.
     */
    @Insert
    suspend fun insertarPedido(pedido: PedidoEntity): Long

    /**
     * Actualiza únicamente el nombre o userId de un pedido existente.
     */
    @Update
    suspend fun actualizarPedido(pedido: PedidoEntity)

    /**
     * Elimina un pedido.
     * (TEN CUIDADO: Room no elimina automáticamente los crossRefs ni los gráficos/hilos
     *  relacionados. Se recomienda borrar primero las relaciones en pedido_grafico_xref
     *  y luego, si se desea, los gráficos huérfanos. Aquí solo eliminamos la fila de pedido.)
     */
    @Delete
    suspend fun eliminarPedido(pedido: PedidoEntity)



    /**
     * Inserta una relación entre un pedido y un gráfico.
     * Si ya existía, se ignora (pasar onConflict = IGNORE si quieres).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCrossRef(crossRef: PedidoGraficoCrossRef)

    /**
     * Borra todas las relaciones entre un pedido y sus gráficos.
     * Útil cuando vamos a actualizar por completo la lista de gráficos de un pedido.
     */
    @Query("DELETE FROM pedido_grafico_xref WHERE pedidoId = :pid")
    suspend fun eliminarCrossRefsDePedido(pid: Int)

    /**
     * Recupera un pedido con todos sus gráficos (y cada gráfico con sus hilos).
     * Devolvemos un Flow para poder observar cambios en tiempo real si se desea.
     */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    fun getPedidoConGraficos(pedidoId: Int): Flow<PedidoWithGraficos?>

    /**
     * Recupera todos los pedidos (sincrona), incluyendo sus gráficos e hilos internos.
     * Normalmente los usaríamos en un ViewModel o repositorio para poblar una lista.
     */
    @Transaction
    @Query("SELECT * FROM pedidos")
    suspend fun getTodosPedidosConGraficos(): List<PedidoWithGraficos>

    /**
     * Busca un pedido por nombre exacto (case-sensitive).
     * Devolvemos null si no existe.
     */
    @Query("SELECT * FROM pedidos WHERE nombre = :nombre LIMIT 1")
    suspend fun getPedidoByNombre(nombre: String): PedidoEntity?
}