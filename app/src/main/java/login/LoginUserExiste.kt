package login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_existe)

        /* en caso de no tener cuenta, redirigir a pantalla de LoginUserNoExiste mediante el textView */
        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
        crearCuenta.setOnClickListener {

            val intent = Intent(this, LoginUserNoExiste::class.java)
            startActivity(intent)
        }

        /* en caso de tenerla, redirigir a la pantalla de inicio */
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener {
            // TODO redirigir a pantalla de Sandra
            // val intent = Intent(this, PantallaInicioActivity::class.java)
            startActivity(intent)
        }
    }
}