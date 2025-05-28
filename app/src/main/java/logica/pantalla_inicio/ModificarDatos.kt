package logica.pantalla_inicio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlyDatabase
import utiles.BaseActivity

class ModificarDatos : BaseActivity() {

    private lateinit var imgOpciones: List<ImageView>
    private lateinit var nombreActualizado: EditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnVolverDatosPersonales: Button

    private var imagenSeleccionada: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_modificar_datos)

        imgOpciones = listOf(
            findViewById(R.id.imgVw_avatar2),
            findViewById(R.id.imgVw_avatar3),
            findViewById(R.id.imgVw_avatar4),
            findViewById(R.id.imgVw_avatar5),
            findViewById(R.id.imgVw_avatar6),
            findViewById(R.id.imgVw_avatar_defecto)
        )

        nombreActualizado = findViewById(R.id.editxtVw_contenidoCambioNombre)
        btnGuardarCambios = findViewById(R.id.btn_GuardarCambios)
        btnVolverDatosPersonales = findViewById(R.id.btn_VolverModificarDatos)

        if (usuarioId < 0) {
            finish()
            return
        }

        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                ThreadlyDatabase.getDatabase(applicationContext)
                    .usuarioDAO()
                    .obtenerPorId(usuarioId)
            }

            if (usuario == null) {
                finish()
                return@launch
            }

            nombreActualizado.hint = usuario.username
            imagenSeleccionada = usuario.profilePic

            imgOpciones.forEach { it.alpha = 0.5f }
            if (imagenSeleccionada in 1..6)
                imgOpciones[imagenSeleccionada - 1].alpha = 1f

            imgOpciones.forEachIndexed { index, img ->
                img.setOnClickListener {
                    imgOpciones.forEach { it.alpha = 0.5f }
                    img.alpha = 1f
                    imagenSeleccionada = index + 1
                }
                img.alpha = 0.5f
            }

            btnGuardarCambios.setOnClickListener {
                val nuevoNombre = nombreActualizado.text.toString().trim()
                val nombreFinal = if (nuevoNombre.isEmpty()) usuario.username else nuevoNombre

                if (nombreFinal == usuario.username && imagenSeleccionada == usuario.profilePic) {
                    Toast.makeText(this@ModificarDatos, "No has hecho ningún cambio.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        ThreadlyDatabase.getDatabase(applicationContext)
                            .usuarioDAO()
                            .actualizar(usuario.copy(
                                username = nombreFinal,
                                profilePic = imagenSeleccionada
                            ))
                    }
                    Toast.makeText(this@ModificarDatos, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnVolverDatosPersonales.setOnClickListener {
            finish()
        }
    }
}
