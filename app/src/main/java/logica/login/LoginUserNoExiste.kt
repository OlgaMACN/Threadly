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
import logica.pantalla_inicio.PantallaPrincipal

/**
 * Actividad que permite a un nuevo usuario registrarse en Threadly.
 * La validación es local y simula la creación de usuarios en memoria.
 * No hay persistencia real, solo lógica de ejemplo.
 */
class LoginUserNoExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView
    private var contrasenaVisible = false

    companion object {
        /**
         * Lista que simula una base de datos en memoria con pares (usuario, contraseña).
         * Se comparte entre instancias mediante un companion object.
         */
        private val usuariosRegistrados = mutableListOf<Pair<String, String>>()
    }

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
                /* verifica si el usuario ya existe (ignorando mayúsculas/minúsculas) */
                val existe = usuariosRegistrados.any { it.first.equals(usuarioEntrada, ignoreCase = true) }
                if (existe) {
                    mostrarToast("Nombre en uso :( Tienes que escoger otro")
                } else {
                    /* agrega nuevo usuario y genera un ID basado en el tamaño de la lista */
                    usuariosRegistrados.add(usuarioEntrada to contrasenaEntrada)

                    val idGenerado = usuariosRegistrados.size

                    val intent = Intent(this@LoginUserNoExiste, PantallaPrincipal::class.java).apply {
                        putExtra("nombre_usuario", usuarioEntrada)
                        putExtra("usuario_id", idGenerado)
                    }
                    startActivity(intent)
                    finish()
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
