package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "hilos_grafico",
    foreignKeys = [ForeignKey(
        entity = GraficoPedidoEntity::class,
        parentColumns = ["id"],
        childColumns = ["graficoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HiloGraficoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val graficoId: Int,
    val codigoHilo: String,
    val cantidad: Int
)
