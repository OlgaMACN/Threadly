package utiles

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    protected var usuarioId: Int = -1
    protected var nombreUsuario: String = "Usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* obtener datos del usuario */
        usuarioId = intent.getIntExtra("usuario_id", -1)
        nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Usuario"

        if (usuarioId < 0) {
            /* si no hay se cierra la sesión */
            finish()
        }
    }

    /* pasar los datos de usuario entre pantallas y evitar cuelgues por pérdida de información */
    fun irAActividad(destino: Class<out BaseActivity>) {
        val intent = Intent(this, destino).apply {
            putExtra("usuario_id", usuarioId)
            putExtra("nombre_usuario", nombreUsuario)
        }
        startActivity(intent)
    }
}
