package logica.login

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
import persistencia.bbdd.ThreadlySingleton
import persistencia.entidades.Usuario

class LoginUserNoExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView
    private var contrasenaVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_no_existe)

        inicializarVistas()
        configurarBotonOjo()
        configurarBotonCrearCuenta()
    }

    private fun inicializarVistas() {
        usuario = findViewById(R.id.edTxt_ingresarNombreNewUser)
        contrasena = findViewById(R.id.edTxt_ingresarConstrasenaNewUser)
        botonOjo = findViewById(R.id.imgVw_eye_closed)
    }

    /* habilitar el efecto de abrir y cerrar el ojo */
    private fun configurarBotonOjo() {
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
    }

    private fun configurarBotonCrearCuenta() {
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener { intentarCrearCuenta() }
    }

    /* crear cuenta y navegar a la pantalla de inicio */
    private fun intentarCrearCuenta() {
        val usuarioEntrada = usuario.text.toString().trim()
        val contrasenaEntrada = contrasena.text.toString().trim()

        /* ni vacío, ni menor a 8 caracteres, ni mayor a 20 */
        when {
            usuarioEntrada.isEmpty() || contrasenaEntrada.isEmpty() -> {
                mostrarToast("Por favor, rellena los campos.")
                contrasena.text.clear()
            }

            usuarioEntrada.length > 20 || contrasenaEntrada.length > 20 -> {
                mostrarToast("Los campos no pueden tener más de 20 caracteres")
                usuario.text.clear()
                contrasena.text.clear()
            }

            contrasenaEntrada.length < 8 -> {
                mostrarToast("Mínimo contraseña: 8 caracteres.")
                contrasena.text.clear()
            }

            else -> {
                /* después de pasar todas las comprobaciones con los toast, se puede crear la cuenta */
                val bbdd = ThreadlySingleton.getDatabase(this)
                val usuarioDao = bbdd.usuarioDao()

                /* coroutine para evitar problemas con los hilos */
                CoroutineScope(Dispatchers.IO).launch {
                    val usuarioExistente = usuarioDao.obtenerPorNombre(usuarioEntrada)

                    /* si el usuario existe no dejará crear otro con el mismo nombre */
                    if (usuarioExistente != null) {
                        withContext(Dispatchers.Main) {
                            mostrarToast("Nombre en uso :( Tienes que escoger otro")
                        }
                    } else {
                        /* si no existe, el registro en la BdD se completará correctamente */
                        val nuevoUsuario =
                            Usuario(nombre = usuarioEntrada, contraseña = contrasenaEntrada)
                        val idGenerado = usuarioDao.insertar(nuevoUsuario).toInt()

                        withContext(Dispatchers.Main) {
                            val intent = Intent(
                                this@LoginUserNoExiste,
                                PantallaPrincipal::class.java
                            ).apply {
                                putExtra("nombre_usuario", usuarioEntrada)
                                putExtra("usuario_id", idGenerado)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    /* sólo para mostrar los toast y reducir código */
    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
