package logica.pantalla_inicio

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.login.LoginUserExiste
import persistencia.bbdd.ThreadlyDatabase
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog

/**
 * Actividad para gestionar y mostrar los datos personales del usuario.
 *
 * En esta pantalla, el usuario puede:
 *  - Ver su nombre de usuario.
 *  - Ver la imagen de perfil seleccionada.
 *  - Modificar sus datos personales.
 *  - Cerrar sesión.
 *  - Eliminar su cuenta.
 *  - Volver a la pantalla principal.
 *
 * Extiende de [BaseActivity] para heredar métodos de navegación y configuración.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class DatosPersonales : BaseActivity() {

    /**
     * TextView que muestra el nombre de usuario en pantalla.
     */
    private lateinit var txtNombreUsuario: TextView

    /**
     * ImageView que muestra el avatar seleccionado por el usuario.
     */
    private lateinit var imgPerfil: ImageView

    /**
     * Botón que lanza la actividad de modificación de datos.
     */
    private lateinit var btnModificarDatos: Button

    /**
     * Botón que permite volver a la pantalla de inicio.
     */
    private lateinit var btnVolverInicio: Button


    /**
     * Punto de entrada de la actividad.
     *
     * - Asigna el layout correspondiente.
     * - Inicializa vistas y botones.
     * - Obtiene y verifica la sesión de usuario; cierra la actividad si no hay sesión.
     * - Configura listeners para modificar datos, cerrar sesión, eliminar cuenta y volver.
     *
     * @param savedInstanceState Bundle con estado previo de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        /* vincular vistas con sus IDs en el layout */
        txtNombreUsuario = findViewById(R.id.txtVw_contenidoNombre)
        imgPerfil = findViewById(R.id.imgVw_fotoPerfil)
        btnModificarDatos = findViewById(R.id.btn_ModificarDatos)
        btnVolverInicio = findViewById(R.id.btn_VolverDatosPersonales)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)
        val btnEliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)

        /* obtener ID de usuario de la sesión activa */
        usuarioId = SesionUsuario.obtenerSesion(this)
        if (usuarioId < 0) {
            /* si no hay usuario válido, finalizar actividad para prevenir errores */
            finish()
            return
        }

        /* modificar datos personales */
        btnModificarDatos.setOnClickListener {
            irAActividad(ModificarDatos::class.java)
        }

        /* cerrar sesión: limpia sesión y lanza LoginUserExiste */
        btnCerrarSesion.setOnClickListener {
            SesionUsuario.cerrarSesion(this)
            val intent = Intent(this, LoginUserExiste::class.java)
            startActivity(intent)
            /* elimina todas las actividades anteriores del stack */
            finishAffinity()
        }

        /* eliminar la cuenta: muestra diálogo de confirmación */
        btnEliminarCuenta.setOnClickListener {
            dialogEliminarCuenta()
        }

        /* volver a la pantalla principal */
        btnVolverInicio.setOnClickListener {
            finish()
        }
    }

    /**
     * Se ejecuta cuando la actividad recupera el foco.
     *
     * Actualiza la interfaz con los datos más recientes del usuario.
     */
    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    /**
     * Carga los datos del usuario (nombre y avatar) desde la base de datos.
     *
     * - Utiliza una corrutina para operaciones de IO.
     * - Si el usuario no existe en la BdD, finaliza la actividad.
     */
    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                ThreadlyDatabase.getDatabase(applicationContext)
                    .usuarioDAO()
                    .obtenerPorId(usuarioId)
            }

            usuario?.let {
                /* asigna el nombre de usuario al TextView */
                txtNombreUsuario.text = it.username
                /* selecciona y asigna el recurso de la imagen de perfil */
                imgPerfil.setImageResource(avatarSeleccionado(it.profilePic))
            } ?: run {
                /* si no se encuentra el usuario en la BdD, cerrar pantalla */
                finish()
            }
        }
    }

    /**
     * Devuelve el ID de recurso drawable correspondiente al avatar indicado.
     *
     * @param id Identificador numérico del avatar en BdD.
     * @return ID de recurso drawable del avatar.
     */
    private fun avatarSeleccionado(id: Int): Int {
        return when (id) {
            1 -> R.drawable.img_avatar2
            2 -> R.drawable.img_avatar3
            3 -> R.drawable.img_avatar4
            4 -> R.drawable.img_avatar5
            5 -> R.drawable.img_avatar6
            else -> R.drawable.img_avatar_defecto
        }
    }

    /**
     * Muestra un diálogo de confirmación para eliminar la cuenta del usuario.
     *
     * Configura botones de cancelar y confirmar:
     * - Cancelar: cierra el diálogo con un Toast de broma.
     * - Confirmar: elimina usuario en BdD, cierra sesión y redirige a login.
     *
     * Utiliza [ajustarDialog] para adaptar el estilo del diálogo.
     */
    private fun dialogEliminarCuenta() {
        /* creación y configuración básica del diálogo */
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pantalla_dialog_eliminar_cuenta)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        /* botones internos del diálogo */
        val btnCancelar = dialog.findViewById<Button>(R.id.btn_Arrepentimiento)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_EliminarCuentaThreadly)

        /* acción de cancelar: feedback y cierre de diálogo */
        btnCancelar.setOnClickListener {
            Toast.makeText(
                this,
                "¡Uff qué susto...! Qué bien que te quedes 🥰",
                Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

        /* acción de confirmar: elimina usuario en BD y redirige al login */
        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ThreadlyDatabase.getDatabase(applicationContext)
                        .usuarioDAO()
                        .eliminarPorId(usuarioId)
                }

                Toast.makeText(
                    this@DatosPersonales,
                    "Tu cuenta se ha eliminado 😢 ¡Hasta pronto!",
                    Toast.LENGTH_LONG
                ).show()

                /* limpia la sesión y navega a pantalla de login */
                SesionUsuario.cerrarSesion(this@DatosPersonales)
                val intent = Intent(this@DatosPersonales, LoginUserExiste::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
        dialog.show()
    }
}
