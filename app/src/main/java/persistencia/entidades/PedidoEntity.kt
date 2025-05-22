package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val fecha: String
)
