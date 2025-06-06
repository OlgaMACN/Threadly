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

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Forzamos modo claro y ocultamos Toolbar
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()

        setContentView(R.layout.splash_layout)
        val logo = findViewById<ImageView>(R.id.logoThreadly)

        val db       = ThreadlyDatabase.getDatabase(applicationContext)
        val usuarioDao = db.usuarioDAO()
        val hiloCatalogoDao = db.hiloCatalogoDao()
        val hiloStockDao   = db.hiloStockDao()

        CoroutineScope(Dispatchers.IO).launch {
            // 1) Si no hay ningún usuario en la tabla, creamos el "prueba"
            val todosUsuarios = usuarioDao.obtenerTodos()
            if (todosUsuarios.isEmpty()) {
                val usuarioPrueba = Usuario(
                    username = "prueba",
                    password = "1234",
                    profilePic = R.drawable.img_avatar_defecto
                )
                val nuevoId = usuarioDao.insertar(usuarioPrueba).toInt()

                // Guardamos la sesión automáticamente para "prueba"
                SesionUsuario.guardarSesion(applicationContext, nuevoId)
            }

            // 2) Animación del logo (solo UI, después de x ms)
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        logo.visibility = View.VISIBLE
                        val fadeIn = AlphaAnimation(0f, 1f)
                        fadeIn.duration = 1000
                        logo.startAnimation(fadeIn)
                    }
                }
            }, 2500)

            // 3) Después de 5s redirigimos según haya sesión o no
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
