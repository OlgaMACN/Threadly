package logica.login

import persistencia.bbdd.GestorBBDD
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.pantalla_inicio.PantallaPrincipal
import persistencia.entidades.Usuario

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
                botonOjo.setImageResource(R.drawable.img_eye_open)
            } else {
                contrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                botonOjo.setImageResource(R.drawable.img_eye_closed)
            }
            contrasena.setSelection(contrasena.text.length)
        }

        /* introducidos los datos, crear cuenta y navegar a la pantalla de inicio */
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener {
            val usuarioEntrada = usuario.text.toString().trim()
            val constrasenaEntrada = contrasena.text.toString().trim()

            /* ni vacío, ni menor a 8 caracteres, ni mayor a 20 */
            if (usuarioEntrada.isEmpty() || constrasenaEntrada.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, rellena los campos.",
                    Toast.LENGTH_SHORT
                ).show()
                contrasena.text.clear()
            } else if (usuarioEntrada.length > 20 || constrasenaEntrada.length > 20) {
                Toast.makeText(
                    this,
                    "Los campos no pueden tener más de 20 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                usuario.text.clear()
                contrasena.text.clear()
            } else if (constrasenaEntrada.length < 8) {
                Toast.makeText(
                    this,
                    "Mínimo contraseña: 8 caracteres.",
                    Toast.LENGTH_SHORT
                ).show()
                contrasena.text.clear()
            } else {
                /* después de pasar todas las comprobaciones con los toast, se puede crear la cuenta */
                val bbdd = GestorBBDD.getDatabase(this)
                val usuarioDao = bbdd.usuarioDao()

                /* ahora, con coroutine para evitar problemas con los hilos */
                CoroutineScope(Dispatchers.IO).launch {
                    val usuarioExistente = usuarioDao.obtenerPorNombre(usuarioEntrada)
                    /* si el usuario existe no dejará crear otro con el mismo nombre */
                    if (usuarioExistente != null) {
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginUserNoExiste,
                                "Nombre en uso :( Tienes que escoger otro",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        /* si no existe, el registro en la BdD se completará correctamente */
                        val nuevoUsuario =
                            Usuario(nombre = usuarioEntrada, contraseña = constrasenaEntrada)
                        val idGenerado =
                            usuarioDao.insertar(nuevoUsuario).toInt()

                        withContext(Dispatchers.Main) {
                            val intent =
                                Intent(this@LoginUserNoExiste, PantallaPrincipal::class.java)
                            intent.putExtra("nombre_usuario", usuarioEntrada)
                            intent.putExtra("usuario_id", idGenerado)
                            startActivity(intent)

                        }
                    }
                }

            } /* usuario con valores correctos */
        } /* fin de la acción del botón 'Entrar' */
    } /* main */
}
