package logica.login

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.pantalla_inicio.PantallaPrincipal
import persistencia.bbdd.GestorBBDD

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

            // todo podría pulir los toast para que sean más concretos, ya para mejoras finales jeje
            /* ni vacío ni mayor a 20 caracteres */
            if (userEntradaLogin.isEmpty() || contrasenaEntradaLogin.isEmpty() || userEntradaLogin.length > 20 || contrasenaEntradaLogin.length > 20) {
                Toast.makeText(
                    this,
                    "Por favor, rellena todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                /* al saltar el toast se borrar los campos*/
                usuario.text.clear()
                contrasena.text.clear()
            } else {
                val bbdd = GestorBBDD.getDatabase(this)
                val usuarioDao = bbdd.usuarioDao()

                /* ahora, con coroutine para evitar problemas con los hilos */
                CoroutineScope(Dispatchers.IO).launch {
                    val usuarioEncontrado = usuarioDao.obtenerPorNombre(userEntradaLogin)
                    if (usuarioEncontrado == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginUserExiste,
                                "El usuario introducido no existe.",
                                Toast.LENGTH_SHORT
                            ).show()
                            usuario.text.clear()
                            contrasena.text.clear()
                        }
                    } else if (usuarioEncontrado.contraseña != contrasenaEntradaLogin) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginUserExiste,
                                "Contraseña incorrecta.",
                                Toast.LENGTH_SHORT
                            ).show()
                            contrasena.text.clear()
                        }
                    } else {
                        /* si se introducen bien el login y la constraseña, el usuario podrá iniciar sesión */
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@LoginUserExiste, PantallaPrincipal::class.java)
                            intent.putExtra("nombre_usuario", userEntradaLogin)
                            intent.putExtra("usuario_id", usuarioEncontrado.id)
                            startActivity(intent)
                        }


                    }
                }

                /* y en caso de no tener cuenta, el usuario hará clic sobre crear cuenta */
                val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
                crearCuenta.setOnClickListener {
                    startActivity(Intent(this, LoginUserNoExiste::class.java))
                }
            } /* usuario con valores correctos */
        } /* fin de la acción del botón 'Entrar' */
    } /* main */
}