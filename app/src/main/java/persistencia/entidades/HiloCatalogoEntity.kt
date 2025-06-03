package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
/*** @author Olga y Sandra Macías Aragón*/
@Entity(
    tableName = "hilo_catalogo",
    /* no se pueden insertar dos hilos con el mismo numHilo para el mismo userId */
    indices = [Index(value = ["userId", "numHilo"], unique = true)]
)
data class HiloCatalogoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val numHilo: String,
    val nombreHilo: String,
    val color: String? = null
)

