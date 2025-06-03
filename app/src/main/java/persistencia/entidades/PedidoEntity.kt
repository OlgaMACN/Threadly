package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "pedidos",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val userId: Int
)
