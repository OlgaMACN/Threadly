package login

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_existe)

        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)

        crearCuenta.setOnClickListener {
            /* en caso de no tener cuenta, redirigir a pantalla de LoginUserNoExiste */
            val intent = Intent(this, LoginUserNoExiste::class.java)
            startActivity(intent)
        }
    }
}