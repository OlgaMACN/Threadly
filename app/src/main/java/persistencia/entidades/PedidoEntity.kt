package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un pedido.
 * - `id`: PK autogenerada.
 * - `nombre`: Nombre único del pedido (por ejemplo “P20230531”).
 * - `userId`: (opcional) para identificar de qué usuario es el pedido.
 */
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val userId: Int
)
