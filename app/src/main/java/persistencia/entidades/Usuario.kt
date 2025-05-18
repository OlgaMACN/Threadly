package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val contraseña: String,
    val idImagen: Int = 1 /* fk a la imagen de perfil, que por defecto la inicial será 1 */
)
