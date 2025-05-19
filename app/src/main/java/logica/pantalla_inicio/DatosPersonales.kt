package logica.pantalla_inicio

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
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
import utiles.ajustarDialog

class DatosPersonales : AppCompatActivity() {

    private lateinit var usuarioDao: UsuarioDAO
    private var usuarioActual: Usuario? = null
    private var nombreUsr: String? = null

    private lateinit var txtNombre: TextView
    private lateinit var imgPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        /* recoger el nombre de usuario desde la pantalla principal */
        nombreUsr = intent.getStringExtra("nombre_usuario")
        if (nombreUsr == null) {
            /* si por lo que sea no llega el nombre de usuario, evitar cierre brusco */
            finish()
            return
        }

        /* declaramos todos los botones de esta pantalla */
        txtNombre = findViewById(R.id.txtVw_contenidoNombre)
        imgPerfil = findViewById(R.id.imgVw_fotoPerfil)
        val btn_modificarDatos = findViewById<Button>(R.id.btn_ModificarDatos)
        val btn_cerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)
        val btn_eliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)
        val btn_volverPantallaInicio = findViewById<Button>(R.id.btn_VolverDatosPersonales)

        /* instanciar  el dao */
        val bbdd = GestorBBDD.getDatabase(this)
        usuarioDao = bbdd.usuarioDao()

        cargarDatos() /* para actualizar los cambios*/

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
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pantalla_dialog_eliminar_cuenta)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            ajustarDialog(dialog)

            dialog.setCancelable(false)

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
                            startActivity(
                                Intent(
                                    this@DatosPersonales,
                                    LoginUserNoExiste::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }
                }
            }

            dialog.show()
        }
        btn_volverPantallaInicio.setOnClickListener { finish() }
    }


    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        nombreUsr?.let { nombre ->
            CoroutineScope(Dispatchers.IO).launch {
                val usuario = usuarioDao.obtenerPorNombre(nombre)
                withContext(Dispatchers.Main) {
                    usuario?.let {
                        usuarioActual = it
                        txtNombre.text = it.nombre
                        /* carga la imagen a través del id */
                        imgPerfil.setImageResource(
                            when (it.idImagen) {
                                1 -> R.drawable.img_avatar_defecto
                                2 -> R.drawable.img_avatar2
                                3 -> R.drawable.img_avatar6
                                4 -> R.drawable.img_avatar3
                                5 -> R.drawable.img_avatar4
                                6 -> R.drawable.img_avatar5
                                else -> R.drawable.img_avatar_defecto
                            }
                        )
                    }
                }
            }
        }
    }
}







