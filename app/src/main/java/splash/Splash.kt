package splash

import login.LoginUserExiste
import java.util.Timer
import java.util.TimerTask
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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

        /* después de 6 segundos, pasar al login */
        Timer().schedule(object : TimerTask() {
            override fun run() {
                startActivity(Intent(this@Splash, LoginUserExiste::class.java))
                finish() // para que el usuario no pueda volver a esta pantalla
            }
        }, 6000)
    }
}