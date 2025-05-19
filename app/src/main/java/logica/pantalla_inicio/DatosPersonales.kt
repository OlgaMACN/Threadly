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
import utiles.ajustarDialog

class DatosPersonales : AppCompatActivity() {

    private lateinit var txtNombreUsuario: TextView
    private lateinit var imgPerfil: ImageView

    /* declaramos todos los botones de esta pantalla */
    private lateinit var btnModificarDatos: Button
    private lateinit var btnVolverInicio: Button

    private lateinit var usuarioDao: UsuarioDAO
    private var usuarioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        txtNombreUsuario = findViewById(R.id.txtVw_contenidoNombre)
        imgPerfil = findViewById(R.id.imgVw_fotoPerfil)
        btnModificarDatos = findViewById(R.id.btn_ModificarDatos)
        btnVolverInicio = findViewById(R.id.btn_VolverDatosPersonales)

        /* instanciar  el dao */
        val bbdd = GestorBBDD.getDatabase(this)
        usuarioDao = bbdd.usuarioDao()

        usuarioId = intent.getIntExtra("usuario_id", -1)
        if (usuarioId < 0) {
            finish()
            return
        }

        /* si se pulsa en 'modificar datos' se mandan los datos a esa pantalla */
        btnModificarDatos.setOnClickListener {
            /* al usuario completo, gracias a la BdD, pasándole el id */
            val intent = Intent(this, ModificarDatos::class.java)
            intent.putExtra("usuario_id", usuarioId)
            startActivity(intent)
        }
        btnVolverInicio.setOnClickListener {
            finish() /* vuelve a pantalla anterior sin perder los cambios */
        }
    }

    override fun onResume() { /* se ejecuta cada vez que vuelve del fondo */
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






