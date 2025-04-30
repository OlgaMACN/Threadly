package Login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserNoExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_no_existe)

        /* TODO dirigir a pantalla de Sandra */
        fun run() {
//            startActivity(Intent(this@LoginUserNoExiste, PrimeraPantalla::class.java))
//            finish() // para que el usuario no pueda volver a esta pantalla
        }
    }
}