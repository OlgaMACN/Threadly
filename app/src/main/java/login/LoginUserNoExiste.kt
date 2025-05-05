package login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserNoExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_no_existe)

        val usuario = findViewById<EditText>(R.id.edTxt_ingresarNombreNewUser)
        val contrasena = findViewById<EditText>(R.id.edTxt_ingresarConstrasenaNewUser)

        /* introducidos los datos, crear cuenta y navegar a la pantalla de inicio */
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
                //TODO redirigir a pantalla de Sandra
               // startActivity(Intent(this, PantallaInicioActivity::class.java))
            }
        }
    }
}