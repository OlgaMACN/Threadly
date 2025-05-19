package utiles

import android.content.Context

object SesionUsuario {
    private const val NOMBRE = "prefs_usuario"
    private const val USER_ID = "usuario_id"

    fun guardarSesion(context: Context, usuarioId: Int) {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        prefs.edit().putInt(USER_ID, usuarioId).apply()
    }

    fun obtenerSesion(context: Context): Int {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        return prefs.getInt(USER_ID, -1)
    }

    fun haySesionActiva(context: Context): Boolean {
        return obtenerSesion(context) != -1
    }

    fun cerrarSesion(context: Context) {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        prefs.edit().remove(USER_ID).apply()
    }
}
