import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogo")
data class Catalogo(
    @PrimaryKey val numHilo: Int,
    val nombreHilo: String,
    val color: String?
)
