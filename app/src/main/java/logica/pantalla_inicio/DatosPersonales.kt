package logica.pantalla_inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.login.LoginUserExiste
import persistencia.bbdd.GestorBBDD
import persistencia.dao.UsuarioDAO
import utiles.BaseActivity
import utiles.SesionUsuario

class DatosPersonales : BaseActivity() {

    private lateinit var txtNombreUsuario: TextView
    private lateinit var imgPerfil: ImageView

    /* declaramos todos los botones de esta pantalla */
    private lateinit var btnModificarDatos: Button
    private lateinit var btnVolverInicio: Button

    private lateinit var usuarioDao: UsuarioDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        txtNombreUsuario = findViewById(R.id.txtVw_contenidoNombre)
        imgPerfil = findViewById(R.id.imgVw_fotoPerfil)
        btnModificarDatos = findViewById(R.id.btn_ModificarDatos)
        btnVolverInicio = findViewById(R.id.btn_VolverDatosPersonales)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)
        val btnEliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)

        /* instanciar  el dao */
        val bbdd = GestorBBDD.getDatabase(this)
        usuarioDao = bbdd.usuarioDao()

        if (usuarioId < 0) {
            finish()
            return
        }

        /* si se pulsa en 'modificar datos' se mandan los datos a esa pantalla */
        btnModificarDatos.setOnClickListener {
            irAActividad(ModificarDatos::class.java)
        }

        /* cerrar sesión */
        btnCerrarSesion.setOnClickListener {
            SesionUsuario.cerrarSesion(this)
            val intent = Intent(this, LoginUserExiste::class.java)
            startActivity(intent)
            finishAffinity() /* cierra todas las actividades abiertas */
        }

        /* eliminar cuenta */
        btnEliminarCuenta.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                usuarioDao.eliminarPorId(usuarioId)
                withContext(Dispatchers.Main) {
                    SesionUsuario.cerrarSesion(this@DatosPersonales)
                    val intent = Intent(this@DatosPersonales, LoginUserExiste::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }

        /* volver a la pantalla inicial */
        btnVolverInicio.setOnClickListener {
            finish() /* vuelve a pantalla anterior sin perder los cambios */
        }
    }

    /* se ejecuta cada vez que vuelve del fondo */
    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        CoroutineScope(Dispatchers.IO).launch {
            val usuario = usuarioDao.obtenerPorId(usuarioId)
            withContext(Dispatchers.Main) {
                usuario?.let {
                    txtNombreUsuario.text = it.nombre
                    imgPerfil.setImageResource(obtenerAvatarDrawable(it.idImagen))
                }
            }
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






