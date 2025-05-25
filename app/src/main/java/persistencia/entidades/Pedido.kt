package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidad que representa un pedido realizado por un usuario en la aplicación Threadly.
 *
 * Un pedido contiene varios gráficos, cada uno con sus hilos asociados.
 * Permite realizar un seguimiento de los hilos necesarios para proyectos específicos.
 *
 * @property pedidoId Identificador único del pedido (clave primaria). Se genera automáticamente.
 * @property userId ID del usuario al que pertenece el pedido (clave foránea hacia Usuario).
 * @property orderName Nombre asignado al pedido (ej. "P2025_1", "Navidad", etc.).
 * @property totalMadejas Número total de madejas necesarias para completar todos los gráficos del pedido.
 * @property resuelto Indica si el pedido ya ha sido procesado (true) o está pendiente (false).
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["userId"],
        childColumns = ["userId"]
    )]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true) val pedidoId: Int = 0,
    val userId: Int,
    val orderName: String,
    val totalMadejas: Int,
    val resuelto: Boolean = false
)
