package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consejos")
data class Consejo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contenido: String
)
