package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey

/* tabla intermedia para los hilos de cada gráfico */
@Entity(
    primaryKeys = ["graphicId", "threadId"],
    foreignKeys = [
        ForeignKey(
            entity = Grafico::class,
            parentColumns = ["graphicId"],
            childColumns = ["graphicId"]
        ),
        ForeignKey(entity = Hilo::class, parentColumns = ["threadId"], childColumns = ["threadId"])
    ]
)
data class GraficoHilo(
    val graphicId: Int,
    val threadId: String,
    val madejas: Int
)

