package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Entidad que representa el stock personal de hilos para cada usuario.
 *
 * Esta tabla es una relación muchos a muchos entre Usuario y Hilo, con información
 * adicional sobre la cantidad de madejas que el usuario tiene en su inventario.
 *
 * @property userId Identificador del usuario propietario del stock (clave foránea hacia Usuario).
 * @property threadId Identificador del hilo almacenado (clave foránea hacia Hilo).
 * @property madejas Número de madejas disponibles para ese hilo en el stock del usuario.
 */
@Entity(
    primaryKeys = ["userId", "threadId"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        ),
        ForeignKey(
            entity = Hilo::class,
            parentColumns = ["threadId"],
            childColumns = ["threadId"]
        )
    ]
)
data class Stock(
    val userId: Int,
    val threadId: String,
    val madejas: Int
)
