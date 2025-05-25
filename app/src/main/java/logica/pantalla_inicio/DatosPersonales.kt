package logica.pantalla_inicio

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.threadly.R
import logica.login.LoginUserExiste
import logica.pantalla_inicio.DatosPersonales.Companion.usuarioEnMemoria
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog

/**
 * Pantalla que permite visualizar y gestionar los datos personales del usuario.
 * Desde aquí se puede modificar el perfil, cerrar sesión o eliminar la cuenta.
 *
 * Esta clase utiliza una variable estática [usuarioEnMemoria] para simular la persistencia del usuario.
 * En una futura implementación se recomienda usar Room.
 */
class DatosPersonales : BaseActivity() {

    private lateinit var txtNombreUsuario: TextView
    private lateinit var imgPerfil: ImageView

    private lateinit var btnModificarDatos: Button
    private lateinit var btnVolverInicio: Button

    companion object {
        /**
         * Representación temporal del usuario en memoria.
         * Reemplazable por una entidad de Room en implementaciones reales.
         */
        var usuarioEnMemoria: UsuarioEnMemoria? = null
    }

    /**
     * Estructura que representa los datos mínimos del usuario para uso temporal en memoria.
     *
     * @param id Identificador del usuario.
     * @param nombre Nombre visible del usuario.
     * @param idImagen Identificador del avatar asociado.
     */
    data class UsuarioEnMemoria(
        val id: Int,
        val nombre: String,
        val idImagen: Int
    )

    /**
     * Método que se ejecuta al crear la actividad. Inicializa componentes visuales y listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        txtNombreUsuario = findViewById(R.id.txtVw_contenidoNombre)
        imgPerfil = findViewById(R.id.imgVw_fotoPerfil)
        btnModificarDatos = findViewById(R.id.btn_ModificarDatos)
        btnVolverInicio = findViewById(R.id.btn_VolverDatosPersonales)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)
        val btnEliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)

        /* si no hay sesión válida, cerrar pantalla */
        if (usuarioId < 0) {
            finish()
            return
        }

        /* inicializa el usuario en memoria si aún no está cargado */
        if (usuarioEnMemoria == null) {
            usuarioEnMemoria = UsuarioEnMemoria(
                id = usuarioId,
                nombre = intent.getStringExtra("nombre_usuario") ?: "Usuario",
                idImagen = 6 // avatar por defecto
            )
        }

        /* botón para modificar datos personales */
        btnModificarDatos.setOnClickListener {
            irAActividad(ModificarDatos::class.java)
        }

        /* botón para cerrar sesión actual */
        btnCerrarSesion.setOnClickListener {
            SesionUsuario.cerrarSesion(this)
            val intent = Intent(this, LoginUserExiste::class.java)
            startActivity(intent)
            finishAffinity()
        }

        /* botón para eliminar cuenta (abre diálogo de confirmación) */
        btnEliminarCuenta.setOnClickListener {
            dialogEliminarCuenta()
        }

        /* botón para volver a la pantalla principal */
        btnVolverInicio.setOnClickListener {
            finish()
        }
    }

    /**
     * Muestra un diálogo personalizado para confirmar la eliminación de la cuenta.
     * Si el usuario confirma, borra los datos en memoria, cierra sesión y vuelve al login.
     */
    private fun dialogEliminarCuenta() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pantalla_dialog_eliminar_cuenta)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnCancelar = dialog.findViewById<Button>(R.id.btn_Arrepentimiento)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_EliminarCuentaThreadly)

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "¡Uff qué susto...! Qué bien que te quedes 🥰", Toast.LENGTH_SHORT)
                .show()
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            dialog.dismiss()

            /* simula eliminación de cuenta */
            usuarioEnMemoria = null

            Toast.makeText(
                this@DatosPersonales,
                "Tu cuenta se ha eliminado 😢 ¡Hasta pronto!",
                Toast.LENGTH_LONG
            ).show()

            SesionUsuario.cerrarSesion(this@DatosPersonales)

            val intent = Intent(this@DatosPersonales, LoginUserExiste::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    /**
     * Se ejecuta al volver a la actividad. Actualiza los datos del usuario en la interfaz.
     */
    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    /**
     * Carga los datos del usuario actual y los muestra en pantalla.
     * Si no hay usuario cargado, se cierra la pantalla.
     */
    private fun cargarDatosUsuario() {
        usuarioEnMemoria?.let {
            txtNombreUsuario.text = it.nombre
            imgPerfil.setImageResource(obtenerAvatarDrawable(it.idImagen))
        } ?: run {
            /* si el usuario no existe, se cierra la pantalla */
            finish()
        }
    }

    /**
     * Obtiene el recurso de imagen correspondiente al ID del avatar seleccionado por el usuario.
     *
     * @param id ID del avatar.
     * @return Recurso drawable correspondiente al avatar.
     */
    private fun obtenerAvatarDrawable(id: Int): Int {
        return when (id) {
            1 -> R.drawable.img_avatar2
            2 -> R.drawable.img_avatar3
            3 -> R.drawable.img_avatar4
            4 -> R.drawable.img_avatar5
            5 -> R.drawable.img_avatar6
            6 -> R.drawable.img_avatar_defecto
            else -> R.drawable.img_avatar_defecto
        }
    }
}
