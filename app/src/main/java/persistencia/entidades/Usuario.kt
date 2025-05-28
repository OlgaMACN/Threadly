package persistencia.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un usuario en la base de datos.
 *
 * Contiene la información básica necesaria para la gestión de usuarios dentro de la aplicación.
 *
 * @property userId Identificador único del usuario (clave primaria autogenerada).
 * @property username Nombre de usuario único para el login.
 * @property password Contraseña del usuario (en texto plano, se recomienda cifrarla antes de almacenar).
 * @property profilePic Identificador del recurso de imagen para el perfil del usuario.
 *
 * * @author Olga y Sandra Macías Aragón
 */
@Entity
data class Usuario(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @ColumnInfo(name = "username") val username: String,
    val password: String,
    val profilePic: Int
)
