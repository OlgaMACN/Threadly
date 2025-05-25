package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Entidad que representa la relación muchos-a-muchos entre gráficos y hilos.
 *
 * Esta tabla intermedia indica qué hilos y cuántas madejas se necesitan
 * para un gráfico de bordado específico.
 *
 * Se usa para modelar que:
 * - Un gráfico puede requerir varios hilos distintos.
 * - Un hilo puede ser utilizado en múltiples gráficos.
 *
 * @property graphicId ID del gráfico asociado (clave foránea hacia [Grafico]).
 * @property threadId ID del hilo necesario (clave foránea hacia [Hilo]).
 * @property madejas Número de madejas requeridas de ese hilo para el gráfico.
 *
 * @see Grafico
 * @see Hilo
 */
@Entity(
    primaryKeys = ["graphicId", "threadId"], /* clave compuesta para evitar duplicados */
    foreignKeys = [
        ForeignKey(
            entity = Grafico::class,
            parentColumns = ["graphicId"],
            childColumns = ["graphicId"],
            onDelete = ForeignKey.CASCADE /* si se borra un gráfico, se eliminan sus asociaciones */
        ),
        ForeignKey(
            entity = Hilo::class,
            parentColumns = ["threadId"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE /* si se borra un hilo del catálogo, se eliminan sus usos en gráficos */
        )
    ]
)
data class GraficoHilo(
    val graphicId: Int,
    val threadId: String,
    val madejas: Int
)
