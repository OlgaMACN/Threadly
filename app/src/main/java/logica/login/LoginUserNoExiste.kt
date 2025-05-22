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

class LoginUserNoExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView
    private var contrasenaVisible = false

    companion object {
        // Simulamos usuarios en memoria (nombre, contraseña)
        private val usuariosRegistrados = mutableListOf<Pair<String, String>>()
    }

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
                val existe = usuariosRegistrados.any { it.first.equals(usuarioEntrada, ignoreCase = true) }
                if (existe) {
                    mostrarToast("Nombre en uso :( Tienes que escoger otro")
                } else {
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

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
