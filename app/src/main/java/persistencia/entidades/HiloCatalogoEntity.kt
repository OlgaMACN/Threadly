package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
/*** @author Olga y Sandra Macías Aragón*/
@Entity(
    tableName = "hilo_catalogo",
    primaryKeys = ["userId", "numHilo"]
)
data class HiloCatalogoEntity(
    val userId: Int,
    val numHilo: String,
    val nombreHilo: String,
    val color: String? = null
)
