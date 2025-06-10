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
 *
 * @author Olga y Sandra Macías Aragón
 *
 * @see Usuario
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
     * Elimina un usuario dado su ID.
     *
     * @param id ID del usuario a eliminar.
     */
    @Query("DELETE FROM usuario WHERE userId = :id")
    suspend fun eliminarPorId(id: Int)

    /**
     * Elimina un usuario dado su ID (alias de eliminarPorId).
     *
     * @param id ID del usuario a eliminar.
     */
    @Query("DELETE FROM usuario WHERE userId = :id")
    suspend fun eliminarUsuarioPorId(id: Int)

    /**
     * Obtiene un usuario por su nombre de usuario.
     *
     * @param nombre Nombre de usuario (username).
     * @return El [Usuario] correspondiente o null si no existe.
     */
    @Query("SELECT * FROM usuario WHERE username = :nombre")
    suspend fun getPorNombre(nombre: String): Usuario?

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

    /**
     * Obtiene la lista completa de usuarios registrados.
     *
     * @return Lista de todos los usuarios.
     */
    @Query("SELECT * FROM Usuario")
    suspend fun obtenerTodos(): List<Usuario>

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario Objeto [Usuario] con datos actualizados.
     */
    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    /**
     * Actualiza los datos de un usuario existente (alias de actualizarUsuario).
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
}
