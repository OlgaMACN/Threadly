package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "graficos",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class GraficoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)