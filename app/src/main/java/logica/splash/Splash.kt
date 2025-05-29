package logica.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logica.login.LoginUserExiste
import logica.pantalla_inicio.PantallaPrincipal
import persistencia.bbdd.ThreadlyDatabase
import persistencia.entidades.Usuario
import utiles.SesionUsuario
import java.util.Timer
import java.util.TimerTask

/**
 * Actividad que muestra la pantalla splash al iniciar la aplicación.
 *
 * Muestra un logo con animación tras un breve retardo, y después de unos segundos
 * redirige al usuario a la pantalla principal si hay sesión activa, o al login en caso contrario.
 *
 * Características:
 * - Forzar modo claro.
 * - Ocultar la barra de título.
 * - Mostrar logo con efecto de desvanecimiento (fade-in)
 * - Transición automática a la pantalla siguiente tras el splash.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class Splash : AppCompatActivity() {

    /**
     * Método llamado al crear la actividad.
     * Configura la vista, animaciones y la transición automática.
     *
     * @param savedInstanceState Bundle con el estado previo, si existiera.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        /* forzar modo claro para toda la actividad */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        /* ocultar la barra de título para una apariencia más limpia */
        supportActionBar?.hide()

        /* habilitar modo edge-to-edge para que la interfaz ocupe toda la pantalla */
        enableEdgeToEdge()

        /* establecer el layout de la actividad con el splash screen */
        setContentView(R.layout.splash_layout)
        val logo = findViewById<ImageView>(R.id.logoThreadly)

        //todo quitar esta parte hasta el comentario masivo en producción
        val db = ThreadlyDatabase.getDatabase(applicationContext)
        val dao = db.usuarioDAO()

        // Crear usuario de prueba si no hay ninguno
        CoroutineScope(Dispatchers.IO).launch {
            val usuarios = dao.obtenerTodos()
            if (usuarios.isEmpty()) {
                val usuarioPrueba = Usuario(
                    username = "prueba",
                    password = "1234",
                    profilePic = R.drawable.img_avatar_defecto
                )
                val id = dao.insertar(usuarioPrueba).toInt()
                SesionUsuario.guardarSesion(applicationContext, id)
            }

            ////////////////////////

            /* crear un temporizador para mostrar el logo con animación después de 2.5 segundos */
            val timerLogo = Timer()
            timerLogo.schedule(object : TimerTask() {
                override fun run() {
                    /* ejecutar en el hilo UI para actualizar la vista */
                    runOnUiThread {
                        logo.visibility = View.VISIBLE
                        val fadeIn = AlphaAnimation(0f, 1f) /* animación de opacidad de 0 a 1 */
                        fadeIn.duration = 1000
                        logo.startAnimation(fadeIn)
                    }
                }
            }, 2500)

            /* crear otro temporizador que ejecuta la transición tras 5 segundos */
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    /* comprobar si hay sesión activa para decidir la pantalla destino */
                    val pantallaProgramada = if (SesionUsuario.haySesionActiva(this@Splash)) {
                        /* si hay sesión activa, ir directamente a la pantalla principal */
                        val intent = Intent(this@Splash, PantallaPrincipal::class.java).apply {
                            /* pasar el id del usuario activo */
                            putExtra("usuario_id", SesionUsuario.obtenerSesion(this@Splash))
                        }
                        intent
                    } else {
                        /* si no hay sesión activa, ir a la pantalla de login */
                        Intent(this@Splash, LoginUserExiste::class.java)
                    }
                    /* iniciar la actividad destino */
                    startActivity(pantallaProgramada)
                    finish()
                }
            }, 5000)
        }
    }
}
