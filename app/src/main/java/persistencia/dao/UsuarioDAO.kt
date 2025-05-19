package persistencia.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Usuario


@Dao
interface UsuarioDAO {
    @Insert
    suspend fun insertar(usuario: Usuario) /* se inserta un nuevo usuario en la BdD */

    @Query("SELECT * FROM Usuario WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Usuario?

    @Query("SELECT * FROM Usuario WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Usuario?

    @Update(onConflict = OnConflictStrategy.FAIL)
    suspend fun actualizar(usuario: Usuario)

    @Delete
    suspend fun eliminar(usuario: Usuario)

}
