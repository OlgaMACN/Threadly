package utiles

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Clase base para actividades que requieren gestión común de usuario.
 *
 * Esta clase extiende de [AppCompatActivity] y proporciona:
 * - Gestión automática de recepción y almacenamiento de datos de usuario (ID y nombre).
 * - Métodos auxiliares para lanzar nuevas actividades que heredan esta base,
 *   asegurando la propagación segura de la información del usuario entre pantallas.
 *
 * Propiedades protegidas:
 * @property usuarioId Identificador del usuario actual. Valor -1 indica sesión inválida.
 * @property nombreUsuario Nombre del usuario actual, por defecto "Usuario".
 *
 * Funciones principales:
 * - [onCreate]: Inicializa la actividad y obtiene datos del usuario del Intent.
 * - [lanzar]: Inicia una nueva actividad que hereda de BaseActivity, pasando datos del usuario y extras opcionales.
 * - [lanzarConResultado]: Similar a lanzar(), pero espera resultado con un requestCode.
 * - [irAActividad]: Método simplificado para iniciar otra actividad heredada de BaseActivity.
 *
 * Si no se recibe un usuario válido (usuarioId < 0), la actividad se cierra automáticamente,
 * simulando el cierre de sesión.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
open class BaseActivity : AppCompatActivity() {

    /** Identificador del usuario actual. -1 indica sesión inválida o no establecida. */
    protected var usuarioId: Int = -1

    /** Nombre del usuario actual, por defecto "Usuario". */
    protected var nombreUsuario: String = "Usuario"

    /**
     * Inicializa la actividad y extrae los datos del usuario del intent.
     * Si no se encuentra un ID válido, finaliza la actividad para cerrar la sesión.
     *
     * @param savedInstanceState Estado previamente guardado, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        /* forzar la orientación vertical */
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)

        /* obtener datos del usuario desde el intent */
        usuarioId = intent.getIntExtra("usuario_id", -1)
        nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Usuario"

        if (usuarioId < 0) {
            /* si no hay usuario válido, cerrar la actividad (cerrar sesión) */
            finish()
        }
    }

    /**
     * Lanza otra actividad que extienda de [BaseActivity], propagando automáticamente
     * el usuarioId y nombreUsuario.
     *
     * @param destino Clase de la actividad destino.
     * @param extras Lambda para añadir más extras al intent si es necesario.
     */
    fun lanzar(destino: Class<out BaseActivity>, extras: Intent.() -> Unit = {}) {
        val i = Intent(this, destino).apply {
            putExtra("usuario_id", usuarioId)
            putExtra("nombre_usuario", nombreUsuario)
            extras() /* permite añadir extras adicionales de forma cómoda */
        }
        startActivity(i)
    }

    /**
     * Lanza otra actividad que extienda de [BaseActivity] esperando un resultado,
     * propagando automáticamente el usuarioId y nombreUsuario.
     *
     * @param destino Clase de la actividad destino.
     * @param requestCode Código para identificar el resultado.
     * @param extras Lambda para añadir más extras al intent si es necesario.
     */
    fun lanzarConResultado(destino: Class<out BaseActivity>, requestCode: Int, extras: Intent.() -> Unit = {}) {
        val i = Intent(this, destino).apply {
            putExtra("usuario_id", usuarioId)
            putExtra("nombre_usuario", nombreUsuario)
            extras()
        }
        startActivityForResult(i, requestCode)
    }

    /**
     * Método simplificado para ir a otra actividad que extienda de [BaseActivity],
     * pasando los datos de usuario para evitar pérdida de información.
     *
     * @param destino Clase de la actividad destino.
     */
    fun irAActividad(destino: Class<out BaseActivity>) {
        val intent = Intent(this, destino).apply {
            putExtra("usuario_id", usuarioId)
            putExtra("nombre_usuario", nombreUsuario)
        }
        startActivity(intent)
    }
}
