package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import logica.pedido_hilos.Grafico

@Dao
interface GraficoDAO {
    @Insert
    suspend fun insertar(grafico: Grafico): Long

    @Update
    suspend fun actualizar(grafico: Grafico)

    @Delete
    suspend fun eliminar(grafico: Grafico)

    @Query("SELECT * FROM Grafico WHERE pedidoId = :pedidoId")
    suspend fun obtenerPorPedido(pedidoId: Int): List<Grafico>

    @Query("SELECT * FROM Grafico WHERE graphicId = :id")
    suspend fun obtenerPorId(id: Int): Grafico?
}