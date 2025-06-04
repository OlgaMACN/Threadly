package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "hilo_stock",
    primaryKeys = ["usuarioId", "hiloId"],
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["userId"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HiloStockEntity(
    val id: Int = 0, // solo informativo
    val usuarioId: Int,
    val hiloId: String,
    val madejas: Int = 0
)

