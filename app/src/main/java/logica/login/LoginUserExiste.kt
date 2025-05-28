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
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.pantalla_inicio.PantallaPrincipal
import persistencia.bbdd.ThreadlyDatabase
import utiles.SesionUsuario

/**
 * Actividad de inicio de sesión para un usuario ya existente.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class LoginUserExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView

    private var contrasenaVisible = false

    /* usuario y contraseña válidos codificados localmente para pruebas */
    private val usuarioValido = "usuarioEjemplo"
    private val contrasenaValida = "12345"

    /**
     * Método que se ejecuta al iniciar la actividad.
     * Se vinculan los elementos del layout y se configuran los botones.
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
     * Inicializa las vistas desde el layout.
     */
    private fun inicializarVistas() {
        usuario = findViewById(R.id.edTxt_ingresarNombreUser)
        contrasena = findViewById(R.id.edTxt_ingresarConstrasenaUser)
        botonOjo = findViewById(R.id.imgVw_eye_closed)
    }

    /**
     * Configura el botón del icono de ojo para mostrar u ocultar la contraseña.
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
     * Configura el botón para iniciar sesión con los datos introducidos.
     */
    private fun configurarBotonEntrar() {
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener { intentarIniciarSesion() }
    }

    /**
     * Configura el texto que lleva a la actividad de crear cuenta nueva.
     */
    private fun configurarCrearCuenta() {
        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
        crearCuenta.setOnClickListener {
            val intent = Intent(this, LoginUserNoExiste::class.java)
            startActivity(intent)
        }
    }

    /**
     * Realiza la validación de los campos y compara los datos con los valores válidos.
     * Si la autenticación es correcta, accede a la pantalla principal.
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
                /* no existe usuario o la contraseña es incorrecta */
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

            /* guardar sesión y continuar */
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
