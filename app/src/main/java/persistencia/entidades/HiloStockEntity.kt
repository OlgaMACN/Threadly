package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey

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
    val usuarioId: Int,
    val hiloId: String,
    val madejas: Int = 0
)

