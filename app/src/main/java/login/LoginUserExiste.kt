package login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_existe)

        val usuario = findViewById<EditText>(R.id.edTxt_ingresarNombreUser)
        val contrasena = findViewById<EditText>(R.id.edTxt_ingresarConstrasenaUser)

        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener {
            val userText = usuario.text.toString().trim()
            val passText = contrasena.text.toString().trim()

            if (userText.isEmpty() || passText.isEmpty() || userText.length > 20 || passText.length > 20) {
                Toast.makeText(
                    this,
                    "Datos inválidos: No se pueden dejar campos en blanco o superar el límite de 20 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                usuario.text.clear()
                contrasena.text.clear()
            } else {
                /* en caso de tener cuenta, redirigir a la pantalla de inicio */
                // TODO redirigir a pantalla de Sandra
                //startActivity(Intent(this, PantallaInicioActivity::class.java))
            }
        }
        /* en caso de no tenerla, redirigir a pantalla de LoginUserNoExiste mediante el textView */
        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
        crearCuenta.setOnClickListener {
            startActivity(Intent(this, LoginUserNoExiste::class.java))
        }
    }
}