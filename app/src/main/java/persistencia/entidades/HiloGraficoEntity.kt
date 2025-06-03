package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hilos_grafico")
data class HiloGraficoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val graficoId: Int,
    val hilo: String,
    val madejas: Int,
    val cantidadModificar: Int? = null
)