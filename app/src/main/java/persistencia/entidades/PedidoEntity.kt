package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un pedido en la base de datos.
 *
 * @property id Identificador único autogenerado del pedido.
 * @property nombre Nombre del pedido. Debe ser único.
 * @property userId Identificador del usuario propietario del pedido.
 * @property realizado Indica si el pedido ha sido marcado como realizado. Por defecto, false.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
@Entity(
    tableName = "pedidos",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val userId: Int,
    val realizado: Boolean = false
)
