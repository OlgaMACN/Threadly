package logica.pantalla_inicio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.threadly.R
import utiles.BaseActivity

/**
 * Actividad que permite al usuario modificar su nombre y avatar.
 * Los datos se actualizan temporalmente en memoria, en la variable compartida [DatosPersonales.usuarioEnMemoria].
 *
 * En futuras versiones, esta funcionalidad debería conectarse a una base de datos con persistencia real.
 */
class ModificarDatos : BaseActivity() {

    private lateinit var imgOpciones: List<ImageView>
    private lateinit var nombreActualizado: EditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnVolverDatosPersonales: Button

    /* avatar seleccionado por el usuario (por defecto: 1). */
    private var imagenSeleccionada: Int = 1

    /**
     * Inicializa la actividad y carga los datos actuales del usuario.
     * También gestiona la selección de avatar y la validación del cambio de nombre.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_modificar_datos)

        /* referencias a los avatares disponibles */
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

        /* validación básica de sesión */
        if (usuarioId < 0 || DatosPersonales.usuarioEnMemoria == null) {
            finish()
            return
        }

        val usuario = DatosPersonales.usuarioEnMemoria!!

        /* mostrar nombre actual como hint */
        nombreActualizado.hint = usuario.nombre
        imagenSeleccionada = usuario.idImagen

        /* mostrar el avatar actual resaltado */
        imgOpciones.forEach { it.alpha = 0.5f }
        if (imagenSeleccionada in 1..6)
            imgOpciones[imagenSeleccionada - 1].alpha = 1f

        /* listener para seleccionar avatar */
        imgOpciones.forEachIndexed { index, img ->
            img.setOnClickListener {
                imgOpciones.forEach { it.alpha = 0.5f }
                img.alpha = 1f
                imagenSeleccionada = index + 1
            }
            img.alpha = 0.5f
        }

        /**
         * Guarda los cambios en la variable `usuarioEnMemoria`.
         * Muestra un mensaje si no se ha cambiado nada.
         */
        btnGuardarCambios.setOnClickListener {
            val nuevoNombre = nombreActualizado.text.toString().trim()
            val nombreFinal = if (nuevoNombre.isEmpty()) usuario.nombre else nuevoNombre

            /* validar si hubo algún cambio real */
            if (nombreFinal == usuario.nombre && imagenSeleccionada == usuario.idImagen) {
                Toast.makeText(this, "No has hecho ningún cambio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            /* actualización simulada en memoria */
            DatosPersonales.usuarioEnMemoria = usuario.copy(
                nombre = nombreFinal,
                idImagen = imagenSeleccionada
            )
            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
            finish()
        }

        /* botón para volver atrás sin guardar */
        btnVolverDatosPersonales.setOnClickListener { finish() }
    }
}
