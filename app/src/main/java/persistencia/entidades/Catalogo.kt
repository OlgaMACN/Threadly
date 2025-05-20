import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(
    tableName = "catalogo",
    indices = [Index(value = ["codigoHilo"], unique = true)]
)
data class Catalogo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "codigoHilo") val codigoHilo: String,
    val nombreHilo: String,
    val color: String?
)
