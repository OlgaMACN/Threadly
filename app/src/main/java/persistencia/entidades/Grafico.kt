package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidad que representa un gráfico de bordado asociado a un pedido.
 *
 * Cada gráfico pertenece a un pedido específico y contiene información básica como
 * el nombre del gráfico y el número de hilos por centímetro (count de la tela).
 *
 * Esta clase forma parte de la estructura de la base de datos Room.
 *
 * @property graphicId ID único del gráfico. Se genera automáticamente.
 * @property pedidoId ID del pedido al que pertenece este gráfico.
 * @property name Nombre descriptivo del gráfico (por ejemplo, "Flores de primavera").
 * @property countTela Count de la tela (por ejemplo, 14 o 18), que indica la densidad de la tela de bordado.
 *
 * @see Pedido
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["pedidoId"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE /* si se borra el pedido, se eliminan sus gráficos */
        )
    ]
)
data class Grafico(
    @PrimaryKey(autoGenerate = true) val graphicId: Int = 0,
    val pedidoId: Int,
    val name: String,
    val countTela: Int
)
