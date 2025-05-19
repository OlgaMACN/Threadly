package logica.pantalla_inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.GestorBBDD
import persistencia.dao.UsuarioDAO
import persistencia.entidades.Usuario

class ModificarDatos : AppCompatActivity() {

    private lateinit var imgOpciones: List<ImageView>
    private lateinit var edtNuevoNombre: EditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnVolverDatosPersonales: Button

    private lateinit var usuarioDao: UsuarioDAO
    private var usuario: Usuario? = null
    private var imagenSeleccionada: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_modificar_datos)

        /* lista para no repetir las mismas operaciones seis veces con cada imagen */
        imgOpciones = listOf(
            findViewById(R.id.imgVw_avatar1),
            findViewById(R.id.imgVw_avatar2),
            findViewById(R.id.imgVw_avatar3),
            findViewById(R.id.imgVw_avatar4),
            findViewById(R.id.imgVw_avatar5),
            findViewById(R.id.imgVw_avatar6)
        )
        /* declaramos componentes de esta pantalla */
        edtNuevoNombre = findViewById(R.id.editxtVw_contenidoCambioNombre)
        btnGuardarCambios = findViewById(R.id.btn_GuardarCambios)
        btnVolverDatosPersonales = findViewById(R.id.btn_VolverModificarDatos)

        /* instancia del DAO */
        val bbdd = GestorBBDD.getDatabase(this)
        usuarioDao = bbdd.usuarioDao()

        /* recoge el id de usuario para obtener sus datos */
        val userId = intent.getIntExtra("usuario_id", -1) /* para depurar errores */
        if (userId < 0) finish() /* si llega mal el usuario se cierra la actividad para evitar cuelgues */

        CoroutineScope(Dispatchers.IO).launch {
            usuario = usuarioDao.obtenerPorId(userId)
            withContext(Dispatchers.Main) {
                usuario?.let {
                    edtNuevoNombre.hint = it.nombre
                    imagenSeleccionada = it.idImagen
                    /* resalta la imagen pulsada */
                    imgOpciones[imagenSeleccionada - 1].alpha = 1f
                }
            }
        }

        /* meramente estético, para destacar la imagen pulsada */
        imgOpciones.forEachIndexed { idx, img ->
            img.setOnClickListener {
                /* todas iguales */
                imgOpciones.forEach { it.alpha = 0.5f }
                /* cambia el alpha de la imagen al seleccionarla */
                img.alpha = 1f
                imagenSeleccionada = idx + 1
            }
            img.alpha = 0.5f
        }

        /* guardar cambios */
        btnGuardarCambios.setOnClickListener {
            val nuevoNombre = edtNuevoNombre.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "Introduce un nuevo nombre.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            usuario?.let { usr ->
                val actualizado = usr.copy(
                    nombre = nuevoNombre,
                    idImagen = imagenSeleccionada
                )
                CoroutineScope(Dispatchers.IO).launch {
                    usuarioDao.actualizar(actualizado)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ModificarDatos,
                            "Datos actualizados", Toast.LENGTH_SHORT
                        ).show()
                        finish()  /* vuelve a la pantalla anterior, con los cambios persistidos */
                    }
                }
            }
        }

        /* volver sin guardar */
        btnVolverDatosPersonales.setOnClickListener { finish() }
    }
}