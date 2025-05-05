package login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserNoExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_no_existe)

        /* introducidos los datos, crear cuenta y navegar a la pantalla de inicio */
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener {
            // TODO redirigir a pantalla de Sandra
            //val intent = Intent(this, PantallaInicioActivity::class.java)
            startActivity(intent)
        }
    }
}