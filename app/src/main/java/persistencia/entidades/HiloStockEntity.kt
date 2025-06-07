package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Entidad que representa el stock de madejas de un hilo específico para un usuario dado.
 *
 * Esta entidad usa una clave primaria compuesta por `usuarioId` y `hiloId` para garantizar
 * que no haya duplicados por usuario y hilo.
 *
 * La relación de clave foránea con la entidad `Usuario` asegura que, al eliminar un usuario,
 * también se eliminen sus registros de stock asociados (borrado en cascada).
 *
 * @property id Campo informativo que no forma parte de la clave primaria.
 * @property usuarioId ID del usuario propietario del stock.
 * @property hiloId Identificador del hilo.
 * @property madejas Cantidad de madejas en stock para ese hilo y usuario.
 *
 * @author Olga y Sandra Macías Aragón
 */
@Entity(
    tableName = "hilo_stock",
    primaryKeys = ["usuarioId", "hiloId"],
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["userId"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HiloStockEntity(
    val id: Int = 0,
    val usuarioId: Int,
    val hiloId: String,
    val madejas: Int = 0
)
