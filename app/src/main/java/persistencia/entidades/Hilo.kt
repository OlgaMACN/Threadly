package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Hilo(
    @PrimaryKey val threadId: String, // Puede ser el código ("310", "ECRU")
    val codigo: String,
    val color: String,
    val numeroPuntadas: Int
)

