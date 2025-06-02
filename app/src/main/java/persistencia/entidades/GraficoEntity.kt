package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "graficos",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class GraficoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val idPedido: Int?, // null = pedido en curso
    val userId: Int      // por si hay varios usuarios
)