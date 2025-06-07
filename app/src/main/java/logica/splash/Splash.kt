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
 * Pantalla inicial de la aplicación (Splash)
 *
 * Esta actividad se lanza al iniciar la app. Tiene tres funciones principales:
 * 1. Forzar el modo claro del dispositivo y ocultar la barra superior.
 * 2. Insertar un usuario de prueba si no hay usuarios registrados en la base de datos.
 * 3. Mostrar una animación de aparición del logo, y después redirigir automáticamente
 *    a la pantalla de inicio o de login según haya sesión activa.
 *
 * El usuario "prueba" se crea con username "prueba" y password "1234" y se guarda la sesión automáticamente.
 * La animación del logo se retrasa 2 segundos y la redirección ocurre a los 5 segundos.
 *
 * @constructor Crea la actividad splash que se ejecuta al iniciar la app.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class Splash : AppCompatActivity() {
// Todo clase en desarollo, en fase producción se eliminará el usuario de prueba
    /**
     * Se ejecuta al crear la actividad. Configura el modo visual, crea el usuario de prueba si es necesario,
     * lanza la animación del logo y redirige al login o pantalla principal después de unos segundos.
     *
     * @param savedInstanceState Estado previamente guardado (no se utiliza en esta implementación realmente).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        /* forzar modo claro y ocultar Toolbar */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()

        setContentView(R.layout.splash_layout)
        val logo = findViewById<ImageView>(R.id.logoThreadly)

        val accesoBdD = ThreadlyDatabase.getDatabase(applicationContext)
        val usuarioDao = accesoBdD.usuarioDAO()

        CoroutineScope(Dispatchers.IO).launch {
            /* si no hay ningún usuario en la tabla, se crea el de "prueba" */
            val usuarios = usuarioDao.obtenerTodos()
            if (usuarios.isEmpty()) {
                val usuarioPrueba = Usuario(
                    username = "prueba",
                    password = "1234",
                    profilePic = R.drawable.img_avatar_defecto
                )
                val nuevoId = usuarioDao.insertar(usuarioPrueba).toInt()

                /* guardar la sesión automáticamente para las pruebas */
                SesionUsuario.guardarSesion(applicationContext, nuevoId)
            }

            /* animación del logo */
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        logo.visibility = View.VISIBLE
                        val fadeIn = AlphaAnimation(0f, 1f)
                        fadeIn.duration = 1000
                        logo.startAnimation(fadeIn)
                    }
                }
            }, 2000)

            /* después de 5s redirigir según haya sesión o no */
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    val destino = if (SesionUsuario.haySesionActiva(this@Splash)) {
                        Intent(this@Splash, PantallaPrincipal::class.java).apply {
                            putExtra("usuario_id", SesionUsuario.obtenerSesion(this@Splash))
                        }
                    } else {
                        Intent(this@Splash, LoginUserExiste::class.java)
                    }
                    startActivity(destino)
                    finish()
                }
            }, 5000)
        }
    }
}
