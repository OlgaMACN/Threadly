package logica.splash

import logica.login.LoginUserExiste
import java.util.Timer
import java.util.TimerTask
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.threadly.R
import logica.pantalla_inicio.PantallaPrincipal
import utiles.SesionUsuario

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        /* para forzar el modo claro */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        /* ocultar la barra del título, más estético */
        supportActionBar?.hide()
        enableEdgeToEdge()
        setContentView(R.layout.splash_layout)
        val logo = findViewById<ImageView>(R.id.logoThreadly)

        /* mostrar el logo después de 2.5 segundos con una animación */
        val timerLogo = Timer()
        timerLogo.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    logo.visibility = View.VISIBLE
                    val fadeIn = AlphaAnimation(0f, 1f)
                    fadeIn.duration = 1000
                    logo.startAnimation(fadeIn)
                }
            }
        }, 2500)

        /* después de 5 segundos, pasar al login */
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val pantallaProgramada = if (SesionUsuario.haySesionActiva(this@Splash)) {
                    /* si el usuario no ha cerrado sesión pasará del splash al login */
                    val intent = Intent(this@Splash, PantallaPrincipal::class.java).apply {
                        putExtra("usuario_id", SesionUsuario.obtenerSesion(this@Splash))
                    }
                    intent
                } else {
                    /* si no se encuentra una sesión activa en el teléfono, al login */
                    Intent(this@Splash, LoginUserExiste::class.java)
                }
                startActivity(pantallaProgramada)
                finish()
            }
        }, 5000)
    }
}