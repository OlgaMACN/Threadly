package login

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class LoginUserNoExiste : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_no_existe)

        val usuario = findViewById<EditText>(R.id.edTxt_ingresarNombreNewUser)
        val contrasena = findViewById<EditText>(R.id.edTxt_ingresarConstrasenaNewUser)

        val botonOjo = findViewById<ImageView>(R.id.imgVw_eye_closed)

        /* para habilitar el efecto de abrir y cerrar el ojo */
        var contrasenaVisible = false
        botonOjo.setOnClickListener {
            contrasenaVisible = !contrasenaVisible
            if (contrasenaVisible) {
                contrasena.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                botonOjo.setImageResource(R.drawable.eye_open)
            } else {
                contrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                botonOjo.setImageResource(R.drawable.eye_closed)
            }
            contrasena.setSelection(contrasena.text.length)
        }

        /* introducidos los datos, crear cuenta y navegar a la pantalla de inicio */
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener {
            val usuarioEntrada = usuario.text.toString().trim()
            val constrasenaEntrada = contrasena.text.toString().trim()

            /* ni vacío ni mayor a 20 caracteres */
            if (usuarioEntrada.isEmpty() || constrasenaEntrada.isEmpty() || usuarioEntrada.length > 20 || constrasenaEntrada.length > 20) {
                Toast.makeText(
                    this,
                    "Por favor, rellena los campos",
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