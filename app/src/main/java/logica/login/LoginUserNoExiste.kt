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
 *
 * Esta pantalla se presenta cuando el usuario aún no tiene cuenta. Permite ingresar un nombre de usuario
 * y una contraseña, valida los datos, y registra al usuario en la base de datos.
 * También gestiona la visibilidad del campo de contraseña mediante un botón con icono de ojo.
 *
 * Si el nombre de usuario no está en uso y los campos cumplen con los requisitos,
 * se crea un nuevo usuario, se guarda la sesión y se redirige a la pantalla principal.
 *
 * @author Olga y Sandra Macías Aragón
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
     * Inicializa los campos de texto e imagen del layout.
     */
    private fun inicializarVistas() {
        usuario = findViewById(R.id.edTxt_ingresarNombreNewUser)
        contrasena = findViewById(R.id.edTxt_ingresarConstrasenaNewUser)
        botonOjo = findViewById(R.id.imgVw_eye_closed)
    }

    /**
     * Configura el botón del ojo para alternar la visibilidad del campo de contraseña.
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
     * Configura el botón que permite registrar una nueva cuenta.
     * Al pulsarlo, se realiza la validación e inserción del usuario.
     */
    private fun configurarBotonCrearCuenta() {
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener { intentarCrearCuenta() }
    }

    /**
     * Intenta registrar un nuevo usuario tras validar los campos introducidos.
     *
     * - Si los campos están vacíos, muestra un mensaje de error.
     * - Si superan el límite de caracteres o la contraseña es demasiado corta, también muestra errores.
     * - Si el nombre de usuario ya existe, solicita uno diferente.
     * - Si es válido, inserta el nuevo usuario en la base de datos, guarda su sesión y abre la pantalla principal.
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

            contrasenaEntrada.length < 5 -> {
                mostrarToast("Mínimo contraseña: 5 caracteres")
                contrasena.text.clear()
            }

            else -> {
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
     * Muestra un mensaje de tipo Toast en la pantalla.
     *
     * @param mensaje El texto que se desea mostrar al usuario.
     */
    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
