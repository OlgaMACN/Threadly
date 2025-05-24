package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(entity = Usuario::class, parentColumns = ["userId"], childColumns = ["userId"])]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true) val pedidoId: Int = 0,
    val userId: Int,
    val orderName: String,
    val totalMadejas: Int,
    val resuelto: Boolean = false // para marcarlo como "procesado"
)

