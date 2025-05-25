package persistencia.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Usuario

/**
 * DAO para la entidad [Usuario].
 *
 * Define las operaciones para insertar, actualizar, eliminar y consultar usuarios en la base de datos.
 */
@Dao
interface UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * La operación aborta si ya existe un usuario con la misma clave primaria.
     *
     * @param usuario Objeto [Usuario] a insertar.
     * @return ID generado del nuevo usuario.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario Objeto [Usuario] con datos actualizados.
     */
    @Update
    suspend fun actualizar(usuario: Usuario)

    /**
     * Elimina un usuario de la base de datos.
     *
     * @param usuario Objeto [Usuario] a eliminar.
     */
    @Delete
    suspend fun eliminar(usuario: Usuario)

    /**
     * Realiza la consulta para login validando usuario y contraseña.
     *
     * @param nombre Nombre de usuario (username).
     * @param clave Contraseña del usuario.
     * @return El [Usuario] que coincide con las credenciales o null si no existe.
     */
    @Query("SELECT * FROM Usuario WHERE username = :nombre AND password = :clave")
    suspend fun login(nombre: String, clave: String): Usuario?

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return El [Usuario] correspondiente o null si no existe.
     */
    @Query("SELECT * FROM Usuario WHERE userId = :id")
    suspend fun obtenerPorId(id: Int): Usuario?

    /**
     * Obtiene un usuario por su nombre de usuario (username).
     *
     * @param nombre Nombre de usuario.
     * @return El [Usuario] correspondiente o null si no existe.
     */
    @Query("SELECT * FROM Usuario WHERE username = :nombre")
    suspend fun obtenerPorNombre(nombre: String): Usuario?
}
