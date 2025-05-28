package logica.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.pantalla_inicio.PantallaPrincipal
import persistencia.bbdd.ThreadlyDatabase
import persistencia.entidades.Usuario
import utiles.SesionUsuario

/**
 * Actividad que permite a un nuevo usuario registrarse en Threadly.
 * * @author Olga y Sandra Macías Aragón
 *
 */
class LoginUserNoExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView
    private var contrasenaVisible = false

    /**
     * Método llamado al iniciar la actividad. Configura las vistas y eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_no_existe)

        inicializarVistas()
        configurarBotonOjo()
        configurarBotonCrearCuenta()
    }

    /**
     * Inicializa las vistas desde el layout XML.
     */
    private fun inicializarVistas() {
        usuario = findViewById(R.id.edTxt_ingresarNombreNewUser)
        contrasena = findViewById(R.id.edTxt_ingresarConstrasenaNewUser)
        botonOjo = findViewById(R.id.imgVw_eye_closed)
    }

    /**
     * Configura el botón para mostrar u ocultar la contraseña escrita.
     */
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

    /**
     * Configura el botón de "crear cuenta" para registrar un nuevo usuario simulado.
     */
    private fun configurarBotonCrearCuenta() {
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener { intentarCrearCuenta() }
    }

    /**
     * Intenta registrar un nuevo usuario validando los campos.
     * Si el nombre está disponible, se guarda en la lista simulada y se inicia la sesión.
     */
    private fun intentarCrearCuenta() {
        val usuarioEntrada = usuario.text.toString().trim()
        val contrasenaEntrada = contrasena.text.toString().trim()

        when {
            usuarioEntrada.isEmpty() || contrasenaEntrada.isEmpty() -> {
                mostrarToast("Por favor, rellena todos los campos")
                contrasena.text.clear()
            }

            usuarioEntrada.length > 20 || contrasenaEntrada.length > 20 -> {
                mostrarToast("Los campos no pueden tener más de 20 caracteres")
                usuario.text.clear()
                contrasena.text.clear()
            }

            contrasenaEntrada.length < 8 -> {
                mostrarToast("Mínimo contraseña: 8 caracteres")
                contrasena.text.clear()
            }

            else -> {
                /* verifica si el usuario ya existe (ignorando mayúsculas/minúsculas) */
                lifecycleScope.launch {
                    val usuarioDAO = ThreadlyDatabase.getDatabase(applicationContext).usuarioDAO()

                    val yaExiste = withContext(Dispatchers.IO) {
                        usuarioDAO.getPorNombre(usuarioEntrada)
                    }

                    if (yaExiste != null) {
                        mostrarToast("Nombre en uso :( Tienes que escoger otro")
                    } else {
                        val nuevoUsuario = Usuario(
                            username = usuarioEntrada,
                            password = contrasenaEntrada,
                            profilePic = R.drawable.img_avatar_defecto
                        )

                        val userId = withContext(Dispatchers.IO) {
                            usuarioDAO.insertar(nuevoUsuario)
                        }

                        /* guarda la sesión a través del id del usuario */
                        SesionUsuario.guardarSesion(applicationContext, userId.toInt())

                        val intent =
                            Intent(this@LoginUserNoExiste, PantallaPrincipal::class.java).apply {
                                putExtra("nombre_usuario", usuarioEntrada)
                                putExtra("usuario_id", userId.toInt())
                            }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    /**
     * Muestra un mensaje Toast corto en pantalla.
     *
     * @param mensaje Texto del mensaje a mostrar.
     */
    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
