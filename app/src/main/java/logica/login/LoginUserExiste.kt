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
import persistencia.bbdd.ThreadlySingleton
import utiles.SesionUsuario

class LoginUserExiste : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var botonOjo: ImageView

    /* la constraseña empieza estando oculta, hasta que el user haga clic en el ojo */
    private var contrasenaVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_existe)

        if (comprobarSesionActiva()) return

        inicializarVistas()
        configurarBotonOjo()
        configurarBotonEntrar()
        configurarCrearCuenta()
    }

    /* comprobar si la sesión está abierta, para acceder directamente sin loguearse */
    private fun comprobarSesionActiva(): Boolean {
        val sesionId = SesionUsuario.obtenerSesion(this)
        if (sesionId != -1) {
            val bbdd = ThreadlySingleton.getDatabase(this)
            val usuarioDao = bbdd.usuarioDao()

            CoroutineScope(Dispatchers.IO).launch {
                val usuario = usuarioDao.obtenerPorId(sesionId)
                withContext(Dispatchers.Main) {
                    if (usuario != null) {
                        val intent =
                            Intent(this@LoginUserExiste, PantallaPrincipal::class.java).apply {
                                putExtra("usuario_id", usuario.id)
                                putExtra("nombre_usuario", usuario.nombre)
                            }
                        startActivity(intent)
                        finish()
                    } else {
                        SesionUsuario.cerrarSesion(this@LoginUserExiste) /* si el usuario ha sido borrado */
                    }
                }
            }
            return true /* no se ejecutará el login si hay sesión activa */
        }
        return false /* se ejecutará el login si no hay sesión activa */
    }

    private fun inicializarVistas() {
        usuario = findViewById(R.id.edTxt_ingresarNombreUser)
        contrasena = findViewById(R.id.edTxt_ingresarConstrasenaUser)
        botonOjo = findViewById(R.id.imgVw_eye_closed)
    }

    /* para habilitar el efecto de abrir y cerrar el ojo */
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
            contrasena.setSelection(contrasena.text.length) /* para poner el cursor de escritura al final */
        }
    }

    /* acción de ingresar a la aplicación, validando los datos */
    private fun configurarBotonEntrar() {
        val btnEntrar = findViewById<Button>(R.id.btn_ingresarThreadly)
        btnEntrar.setOnClickListener { intentarIniciarSesion() }
    }

    private fun configurarCrearCuenta() {
        val crearCuenta = findViewById<TextView>(R.id.txtVw_crearCuenta)
        crearCuenta.setOnClickListener {
            startActivity(Intent(this, LoginUserNoExiste::class.java))
        }
    }

    /* acción de ingresar a la aplicación, validando los datos */
    private fun intentarIniciarSesion() {
        val userEntrada = usuario.text.toString().trim()
        val contrasenaEntrada = contrasena.text.toString().trim()

        /* ni vacío ni mayor a 20 caracteres */
        if (userEntrada.isEmpty() || contrasenaEntrada.isEmpty() || userEntrada.length > 20 || contrasenaEntrada.length > 20) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            /* al saltar el toast se borrar los campos*/
            usuario.text.clear()
            contrasena.text.clear()
            return
        }

        val bbdd = ThreadlySingleton.getDatabase(this)
        val usuarioDao = bbdd.usuarioDao()

        /* coroutine para evitar problemas con los hilos */
        CoroutineScope(Dispatchers.IO).launch {
            val usuarioEncontrado = usuarioDao.obtenerPorNombre(userEntrada)
            withContext(Dispatchers.Main) {
                when {
                    usuarioEncontrado == null -> {
                        Toast.makeText(
                            this@LoginUserExiste,
                            "El usuario introducido no existe.",
                            Toast.LENGTH_SHORT
                        ).show()
                        usuario.text.clear()
                        contrasena.text.clear()
                    }

                    usuarioEncontrado.contraseña != contrasenaEntrada -> {
                        Toast.makeText(
                            this@LoginUserExiste,
                            "Contraseña incorrecta.",
                            Toast.LENGTH_SHORT
                        ).show()
                        contrasena.text.clear()
                    }

                    else -> {
                        /* si se introducen bien el login y la constraseña, el usuario podrá iniciar sesión */
                        SesionUsuario.guardarSesion(this@LoginUserExiste, usuarioEncontrado.id)
                        val intent =
                            Intent(this@LoginUserExiste, PantallaPrincipal::class.java).apply {
                                putExtra("nombre_usuario", userEntrada)
                                putExtra("usuario_id", usuarioEncontrado.id)
                            }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
