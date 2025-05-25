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
import logica.pantalla_inicio.PantallaPrincipal

/**
 * Actividad de inicio de sesión para un usuario ya existente.
 * Simula un sistema básico de autenticación con validación local.
 *
 * Esta clase no utiliza persistencia real, por lo que solo sirve como ejemplo
 * de flujo de login. El usuario válido está codificado de forma fija.
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

        if (userEntrada != usuarioValido) {
            Toast.makeText(this, "El usuario introducido no existe.", Toast.LENGTH_SHORT).show()
            usuario.text.clear()
            contrasena.text.clear()
            return
        }

        if (contrasenaEntrada != contrasenaValida) {
            Toast.makeText(this, "Contraseña incorrecta.", Toast.LENGTH_SHORT).show()
            contrasena.text.clear()
            return
        }

        /* si la autenticación es correcta, el usuario accede a la pantalla principal */
        val intent = Intent(this@LoginUserExiste, PantallaPrincipal::class.java).apply {
            putExtra("nombre_usuario", userEntrada)
            putExtra("usuario_id", 1) /* id ficticio */
        }
        startActivity(intent)
        finish()
    }
}
