package utiles

import android.content.Context

/**
 * Objeto singleton que gestiona la sesión del usuario usando SharedPreferences.
 *
 * Proporciona métodos para guardar, obtener, comprobar y cerrar la sesión
 * mediante el almacenamiento del ID del usuario en preferencias privadas.
 *
 * Evita acceder a la BdD cada vez que se abra la app.
 *
 * * @author Olga y Sandra Macías Aragón
 *
 */
object SesionUsuario {

    private const val NOMBRE = "prefs_usuario"
    private const val USER_ID = "usuario_id"

    /**
     * Guarda el ID del usuario en SharedPreferences, iniciando la sesión.
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @param usuarioId ID del usuario que inicia sesión.
     */
    fun guardarSesion(context: Context, usuarioId: Int) {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        prefs.edit().putInt(USER_ID, usuarioId).apply()
    }

    /**
     * Obtiene el ID del usuario almacenado en sesión.
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @return ID del usuario si hay sesión, o -1 si no hay sesión activa.
     */
    fun obtenerSesion(context: Context): Int {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        return prefs.getInt(USER_ID, -1)
    }

    /**
     * Comprueba si hay una sesión activa (ID de usuario válido almacenado).
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @return true si hay sesión activa, false en caso contrario.
     */
    fun haySesionActiva(context: Context): Boolean {
        return obtenerSesion(context) != -1
    }

    /**
     * Cierra la sesión borrando el ID del usuario almacenado.
     *
     * @param context Contexto para acceder a SharedPreferences.
     */
    fun cerrarSesion(context: Context) {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        prefs.edit().remove(USER_ID).apply()
    }
}
