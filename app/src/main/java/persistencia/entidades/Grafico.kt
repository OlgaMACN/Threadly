package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(entity = Pedido::class, parentColumns = ["pedidoId"], childColumns = ["pedidoId"])]
)
data class Grafico(
    @PrimaryKey(autoGenerate = true) val graphicId: Int = 0,
    val pedidoId: Int,
    val name: String,
    val countTela: Int
)

