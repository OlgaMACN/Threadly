package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "graficos_pedido",
    foreignKeys = [ForeignKey(
        entity = PedidoEntity::class,
        parentColumns = ["id"],
        childColumns = ["pedidoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GraficoPedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pedidoId: Int,
    val nombreGrafico: String
)
