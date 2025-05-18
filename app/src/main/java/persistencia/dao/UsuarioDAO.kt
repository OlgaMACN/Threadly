package persistencia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import persistencia.entidades.Usuario


@Dao
interface UsuarioDAO {
    @Insert
    suspend fun insertar(usuario: Usuario) /* se inserta un nuevo usuario en la BdD */

    @Query("SELECT * FROM Usuario WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Usuario?

}
