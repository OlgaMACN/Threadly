package persistencia.entidades

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["userId", "threadId"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["userId"], childColumns = ["userId"]),
        ForeignKey(entity = Hilo::class, parentColumns = ["threadId"], childColumns = ["threadId"])
    ]
)
data class Stock(
    val userId: Int,
    val threadId: String,
    val madejas: Int
)
