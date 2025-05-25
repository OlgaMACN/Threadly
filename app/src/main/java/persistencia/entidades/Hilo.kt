package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un hilo del catálogo principal de Threadly.
 *
 * Cada hilo contiene información identificativa, visual y técnica.
 * Se utiliza como base para los inventarios personales, gráficos, y pedidos.
 *
 * @property threadId Identificador único del hilo (clave primaria), puede ser un código como "310" o "ECRU".
 * @property codigo Código del hilo (usualmente coincide con el threadId, pero se puede mantener separado para claridad o normalización).
 * @property color color del hilo (por ejemplo: "Negro", "Beige claro").
 * @property numeroPuntadas Número de puntadas que se pueden realizar con una madeja de este hilo (dato técnico para calcular necesidades).
 */
@Entity
data class Hilo(
    @PrimaryKey val threadId: String,
    val codigo: String,
    val color: String,
    val numeroPuntadas: Int
)
