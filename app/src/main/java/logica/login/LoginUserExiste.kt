package logica.login

import LoginUserNoExiste
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.pantalla_inicio.PantallaPrincipal
import persistencia.bbdd.ThreadlyDatabase
import utiles.SesionUsuario

/**
 * Actividad de inicio de sesión para usuarios existentes en la aplicación.
 *
 * Permite ingresar el nombre de usuario y la contraseña, validar las credenciales
 * contra la base de datos local Room y acceder a la pantalla principal en caso de éxito.
 * También permite alternar la visibilidad de la contraseña y navegar a la pantalla de
 * creación de cuenta nueva si el usuario no está registrado.
 *
 *  * @author Olga y Sandra Macías Aragón
 */
class LoginUserExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView

    private var contrasenaVisible = false

    /**
     * Método que se ejecuta al crear la actividad.
     * Inicializa las vistas, configura los botones e interacciones del usuario.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad (no utilizado aquí).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_existe)

        inicializarVistas()
        configurarBotonOjo()
        configurarBotonEntrar()
        configurarCrearCuenta()
    }

    /**
     * Asigna las vistas del layout a variables locales.
     * Estas vistas incluyen los campos de entrada para el nombre de usuario y contraseña,
     * y el botón/ícono de mostrar/ocultar contraseña.
     */
    private fun inicializarVistas() {
        usuario = findViewById(R.id.edTxt_ingresarNombreUser)
        contrasena = findViewById(R.id.edTxt_ingresarConstrasenaUser)
        botonOjo = findViewById(R.id.imgVw_eye_closed)
    }

    /**
     * Configura el comportamiento del botón de "ojo" que permite mostrar u ocultar
     * el contenido del campo de contraseña.
     *
     * Cambia dinámicamente el tipo de entrada del campo y la imagen del ícono.
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
     * Configura el botón de inicio de sesión.
     * Llama al método [intentarIniciarSesion] al hacer clic.
     */
    private fun configurarBotonEntrar() {
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener { intentarIniciarSesion() }
    }

    /**
     * Configura el texto que permite cambiar a la pantalla de creación de cuenta
     * si el usuario no está registrado.
     */
    private fun configurarCrearCuenta() {
        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
        crearCuenta.setOnClickListener {
            val intent = Intent(this, LoginUserNoExiste::class.java)
            startActivity(intent)
        }
    }

    /**
     * Intenta autenticar al usuario con las credenciales introducidas.
     *
     * - Valida que los campos no estén vacíos ni excedan los 20 caracteres.
     * - Consulta en la base de datos si las credenciales son correctas.
     * - Si lo son, guarda la sesión y redirige a [PantallaPrincipal].
     * - Si el usuario no existe o la contraseña es incorrecta, muestra mensajes adecuados.
     */
    private fun intentarIniciarSesion() {
        val userEntrada = usuario.text.toString().trim()
        val contrasenaEntrada = contrasena.text.toString().trim()

        if (userEntrada.isEmpty() || contrasenaEntrada.isEmpty() || userEntrada.length > 20 || contrasenaEntrada.length > 20) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            usuario.text.clear()
            contrasena.text.clear()
            return
        }

        lifecycleScope.launch {
            val usuarioDAO = ThreadlyDatabase.getDatabase(applicationContext).usuarioDAO()

            val usuarioBD = withContext(Dispatchers.IO) {
                usuarioDAO.login(userEntrada, contrasenaEntrada)
            }

            if (usuarioBD == null) {
                /* el usuario no existe o la contraseña es incorrecta */
                val existeUsuario = withContext(Dispatchers.IO) {
                    usuarioDAO.getPorNombre(userEntrada)
                }
                if (existeUsuario == null) {
                    Toast.makeText(
                        this@LoginUserExiste,
                        "El usuario introducido no existe",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginUserExiste,
                        "Contraseña incorrecta",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            /* guardar sesión y continuar a la pantalla principal */
            SesionUsuario.guardarSesion(applicationContext, usuarioBD.userId)

            val intent = Intent(this@LoginUserExiste, PantallaPrincipal::class.java).apply {
                putExtra("nombre_usuario", usuarioBD.username)
                putExtra("usuario_id", usuarioBD.userId)
            }
            startActivity(intent)
            finish()
        }
    }
}
