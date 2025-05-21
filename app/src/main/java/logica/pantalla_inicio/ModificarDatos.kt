package logica.pantalla_inicio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.threadly.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlySingleton
import persistencia.dao.UsuarioDAO
import persistencia.entidades.Usuario
import utiles.BaseActivity

class ModificarDatos : BaseActivity() {

    private lateinit var imgOpciones: List<ImageView>
    private lateinit var nombreActualizado: EditText
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
            findViewById(R.id.imgVw_avatar2),
            findViewById(R.id.imgVw_avatar3),
            findViewById(R.id.imgVw_avatar4),
            findViewById(R.id.imgVw_avatar5),
            findViewById(R.id.imgVw_avatar6),
            findViewById(R.id.imgVw_avatar_defecto)
        )
        /* declaramos componentes de esta pantalla */
        nombreActualizado = findViewById(R.id.editxtVw_contenidoCambioNombre)
        btnGuardarCambios = findViewById(R.id.btn_GuardarCambios)
        btnVolverDatosPersonales = findViewById(R.id.btn_VolverModificarDatos)

        /* instancia del DAO */
        val bbdd = ThreadlySingleton.getDatabase(this)
        usuarioDao = bbdd.usuarioDao()

        /* si llega mal el usuario se cierra la actividad para evitar cuelgues */
        if (usuarioId < 0) {
            finish()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            usuario = usuarioDao.obtenerPorId(usuarioId)
            withContext(Dispatchers.Main) {
                usuario?.let {
                    nombreActualizado.hint = it.nombre
                    imagenSeleccionada = it.idImagen
                    /* resalta la imagen pulsada */
                    imgOpciones.forEach { img -> img.alpha = 0.5f }
                    if (imagenSeleccionada in 1..6)
                        imgOpciones[imagenSeleccionada - 1].alpha = 1f
                }
            }
        }

        /* meramente estético, para destacar la imagen pulsada */
        imgOpciones.forEachIndexed { index, img ->
            img.setOnClickListener {
                /* todas iguales */
                imgOpciones.forEach { it.alpha = 0.5f }
                /* cambia el alpha de la imagen al seleccionarla */
                img.alpha = 1f
                imagenSeleccionada = index + 1
            }
            img.alpha = 0.5f
        }

        /* guardar cambios */
        btnGuardarCambios.setOnClickListener {
            val nuevoNombre = nombreActualizado.text.toString().trim()
            /*  if (nuevoNombre.isEmpty()) {
                  Toast.makeText(this, "Introduce un nuevo nombre.", Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }*/

            usuario?.let { user ->
                /* TODO para que no obligue a cambiar el nombre, no? */
                val nombreFinal = if (nuevoNombre.isEmpty()) user.nombre else nuevoNombre

                if (nombreFinal == user.nombre && imagenSeleccionada == user.idImagen) {
                    Toast.makeText(this, "No has hecho ningún cambio.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val actualizado = user.copy(
                    nombre = nombreFinal,
                    idImagen = imagenSeleccionada
                )

                CoroutineScope(Dispatchers.IO).launch {
                    usuarioDao.actualizar(actualizado)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ModificarDatos,
                            "Datos actualizados", Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }

        /* volver sin guardar */
        btnVolverDatosPersonales.setOnClickListener { finish() }
    }
}