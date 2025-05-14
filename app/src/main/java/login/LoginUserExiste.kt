package login

import PantallaInicio.PantallaPrincipal
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
        val botonOjo = findViewById<ImageView>(R.id.imgVw_eye_closed)

        /* para habilitar el efecto de abrir y cerrar el ojo */
        var contrasenaVisible =
            false /* la constraseña empieza estando oculta, hasta que el user haga clic en el ojo */
        botonOjo.setOnClickListener {
            contrasenaVisible = !contrasenaVisible
            if (contrasenaVisible) {
                contrasena.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                botonOjo.setImageResource(R.drawable.img_eye_open)
            } else {
                contrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                botonOjo.setImageResource(R.drawable.img_eye_closed)
            }
            contrasena.setSelection(contrasena.text.length) /* para poner el cursor de escritura al final */
        }

        /* acción de ingresar a la aplicación, validando los datos */
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener {
            val userEntradaLogin = usuario.text.toString().trim()
            val contrasenaEntradaLogin = contrasena.text.toString().trim()

            /* ni vacío ni mayor a 20 caracteres */
            if (userEntradaLogin.isEmpty() || contrasenaEntradaLogin.isEmpty() || userEntradaLogin.length > 20 || contrasenaEntradaLogin.length > 20) {
                Toast.makeText(
                    this,
                    "Por favor, rellena los campos",
                    Toast.LENGTH_SHORT
                ).show()
                /* al saltar el toast se borrar los campos*/
                usuario.text.clear()
                contrasena.text.clear()
            } else {
                 startActivity(Intent(this, PantallaPrincipal::class.java))
            }
        }
        /* en caso de no tenerla, redirigir a pantalla de LoginUserNoExiste mediante el textView */
        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
        crearCuenta.setOnClickListener {
            startActivity(Intent(this, LoginUserNoExiste::class.java))
        }
    }
}