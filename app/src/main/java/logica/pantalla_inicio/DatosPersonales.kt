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
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ajustarDialog

class DatosPersonales : BaseActivity() {

    private lateinit var txtNombreUsuario: TextView
    private lateinit var imgPerfil: ImageView

    private lateinit var btnModificarDatos: Button
    private lateinit var btnVolverInicio: Button

    companion object {
        // Simulación de usuario en memoria
        var usuarioEnMemoria: UsuarioEnMemoria? = null
    }

    data class UsuarioEnMemoria(
        val id: Int,
        val nombre: String,
        val idImagen: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        txtNombreUsuario = findViewById(R.id.txtVw_contenidoNombre)
        imgPerfil = findViewById(R.id.imgVw_fotoPerfil)
        btnModificarDatos = findViewById(R.id.btn_ModificarDatos)
        btnVolverInicio = findViewById(R.id.btn_VolverDatosPersonales)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)
        val btnEliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)

        if (usuarioId < 0) {
            finish()
            return
        }

        // Inicializar usuario en memoria si es null (simulación)
        if (usuarioEnMemoria == null) {
            usuarioEnMemoria = UsuarioEnMemoria(
                id = usuarioId,
                nombre = intent.getStringExtra("nombre_usuario") ?: "Usuario",
                idImagen = 6 // avatar por defecto
            )
        }

        btnModificarDatos.setOnClickListener {
            irAActividad(ModificarDatos::class.java)
        }

        btnCerrarSesion.setOnClickListener {
            SesionUsuario.cerrarSesion(this)
            val intent = Intent(this, LoginUserExiste::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnEliminarCuenta.setOnClickListener {
            dialogEliminarCuenta()
        }

        btnVolverInicio.setOnClickListener {
            finish()
        }
    }

    private fun dialogEliminarCuenta() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pantalla_dialog_eliminar_cuenta)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnCancelar = dialog.findViewById<Button>(R.id.btn_Arrepentimiento)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btn_EliminarCuentaThreadly)

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "¡Uff qué susto...! Qué bien que te quedes \uD83E\uDD70", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            dialog.dismiss()

            // Simulamos borrar el usuario en memoria
            usuarioEnMemoria = null

            Toast.makeText(this@DatosPersonales, "Tu cuenta se ha eliminado \uD83D\uDE22 ¡Hasta pronto!", Toast.LENGTH_LONG).show()

            SesionUsuario.cerrarSesion(this@DatosPersonales)

            val intent = Intent(this@DatosPersonales, LoginUserExiste::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        usuarioEnMemoria?.let {
            txtNombreUsuario.text = it.nombre
            imgPerfil.setImageResource(obtenerAvatarDrawable(it.idImagen))
        } ?: run {
            // Si no hay usuario en memoria, cerrar pantalla o manejar error
            finish()
        }
    }

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
