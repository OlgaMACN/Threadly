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

class DatosPersonales : BaseActivity() {

    private lateinit var txtNombreUsuario: TextView
    private lateinit var imgPerfil: ImageView

    private lateinit var btnModificarDatos: Button
    private lateinit var btnVolverInicio: Button

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

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                ThreadlyDatabase.getDatabase(applicationContext)
                    .usuarioDAO()
                    .obtenerPorId(usuarioId)
            }

            usuario?.let {
                txtNombreUsuario.text = it.username
                imgPerfil.setImageResource(obtenerAvatarDrawable(it.profilePic))
            } ?: finish()
        }
    }

    private fun obtenerAvatarDrawable(id: Int): Int {
        return when (id) {
            1 -> R.drawable.img_avatar2
            2 -> R.drawable.img_avatar3
            3 -> R.drawable.img_avatar4
            4 -> R.drawable.img_avatar5
            5 -> R.drawable.img_avatar6
            else -> R.drawable.img_avatar_defecto
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
            Toast.makeText(this, "¡Uff qué susto...! Qué bien que te quedes 🥰", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

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

                SesionUsuario.cerrarSesion(this@DatosPersonales)

                val intent = Intent(this@DatosPersonales, LoginUserExiste::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        dialog.show()
    }
}
