package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Usuario

@Dao
interface UsuarioDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Delete
    suspend fun eliminar(usuario: Usuario)

    @Query("SELECT * FROM Usuario WHERE username = :nombre AND password = :clave")
    suspend fun login(nombre: String, clave: String): Usuario?

    @Query("SELECT * FROM Usuario WHERE userId = :id")
    suspend fun obtenerPorId(id: Int): Usuario?

    @Query("SELECT * FROM Usuario WHERE username = :nombre")
    suspend fun obtenerPorNombre(nombre: String): Usuario?
}
