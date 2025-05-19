package logica.pantalla_inicio

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.login.LoginUserExiste
import logica.login.LoginUserNoExiste
import persistencia.bbdd.GestorBBDD
import persistencia.dao.UsuarioDAO
import persistencia.entidades.Usuario

class DatosPersonales : AppCompatActivity() {

    private lateinit var usuarioDao: UsuarioDAO
    private var usuarioActual: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        /* declaramos todos los botones de esta pantalla */
        var txtNombre = findViewById<TextView>(R.id.txtVw_contenidoNombre)
        var imgPerfil = findViewById<ImageView>(R.id.imgVw_fotoPerfil)
        val btn_modificarDatos = findViewById<Button>(R.id.btn_ModificarDatos)
        val btn_cerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)
        val btn_eliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)
        val btn_volverPantallaInicio = findViewById<Button>(R.id.btn_VolverDatosPersonales)

        /* instanciar  el dao */
        val bbdd = GestorBBDD.getDatabase(this)
        usuarioDao = bbdd.usuarioDao()

        /* cargar datos del usuario */
        val nombreUsr = intent.getStringExtra("nombre_usuario") ?: return
        CoroutineScope(Dispatchers.IO).launch {
            usuarioActual = usuarioDao.obtenerPorNombre(nombreUsr)
            withContext(Dispatchers.Main) {
                usuarioActual?.let {
                    txtNombre.text = it.nombre
                    /* carga la imagen a través del id */
                    imgPerfil.setImageResource(
                        when (it.idImagen) {
                            1 -> R.drawable.img_avatar1
                            2 -> R.drawable.img_avatar2
                            3 -> R.drawable.img_avatar3
                            4 -> R.drawable.img_avatar4
                            5 -> R.drawable.img_avatar5
                            6 -> R.drawable.img_avatar6
                            else -> R.drawable.img_avatar1
                        }
                    )
                }
            }
        }

        /* si se pulsa en 'modificar datos' se mandan los datos a esa pantalla */
        btn_modificarDatos.setOnClickListener {
            /* al usuario completo, gracias a la BdD, pasándole el id */
            val i = Intent(this, ModificarDatos::class.java)
            i.putExtra("usuario_id", usuarioActual?.id)
            startActivity(i)
        }

        /* cerrar sesión */
        btn_cerrarSesion.setOnClickListener {
            /* simplemente vuelve al login */
            startActivity(Intent(this, LoginUserExiste::class.java))
            finishAffinity()
        }

        /* eliminar cuenta */
        btn_eliminarCuenta.setOnClickListener() {
            val dialog = Dialog(this).apply {
                setContentView(R.layout.pantalla_dialog_eliminar_cuenta)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setCancelable(false)
            }

            dialog.findViewById<Button>(R.id.btn_Arrepentimiento).setOnClickListener {
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Ufff, menudo susto... ¡menos mal que te quedas! 🥰",
                    Toast.LENGTH_LONG
                ).show()
            }

            dialog.findViewById<Button>(R.id.btn_EliminarCuentaThreadly).setOnClickListener {
                usuarioActual?.let { user ->
                    CoroutineScope(Dispatchers.IO).launch {
                        usuarioDao.eliminar(user)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@DatosPersonales,
                                "Se ha eliminado tu cuenta, hasta pronto… 😢",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(Intent(this@DatosPersonales, LoginUserNoExiste::class.java))
                            finishAffinity()
                        }
                    }
                }
            }
            dialog.show()
        }

        /* volver sin guardar datos de cambio */
        btn_volverPantallaInicio.setOnClickListener { finish() }
    }
}