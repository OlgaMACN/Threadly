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

/**
 * Actividad que permite al usuario modificar su nombre de usuario y avatar.
 *
 * Ofrece las siguientes funcionalidades:
 *  - Mostrar el nombre y avatar actuales.
 *  - Permitir elegir entre varios avatares disponibles.
 *  - Actualizar el nombre y/o avatar si hay cambios.
 *  - Avisar si no se detectan modificaciones.
 *  - Permitir volver a la pantalla de datos personales sin guardar.
 *
 * Extiende de [BaseActivity] para heredar la gestión de sesión y navegación.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class ModificarDatos : BaseActivity() {

    /**
     * Lista de ImageView que representa las opciones de avatar
     */
    private lateinit var imgOpciones: List<ImageView>

    /**
     * EditText para ingresar el nuevo nombre de usuario.
     * Si permanece vacío, se mantiene el nombre actual
     */
    private lateinit var nombreActualizado: EditText

    /**
     * Botón que confirma y guarda los cambios realizados
     */
    private lateinit var btnGuardarCambios: Button

    /**
     * Botón que cancela la modificación y regresa a DatosPersonales
     */
    private lateinit var btnVolverDatosPersonales: Button

    /**
     * Identifica el avatar actualmente seleccionado (1..6).
     * Inicializado con el valor por defecto hasta cargar el usuario
     */
    private var imagenSeleccionada: Int = 1

    /**
     * Método principal de inicialización de la actividad.
     *
     * - Asocia el layout de modificación.
     * - Encuentra y asigna vistas y botones.
     * - Verifica sesión activa; cierra si no existe.
     * - Carga datos del usuario (nombre y avatar) en corrutina.
     * - Inicializa estados gráficos y listeners para la selección de avatar.
     * - Configura el listener para guardar cambios.
     * - Configura el listener para volver sin guardar.
     *
     * @param savedInstanceState Bundle con estado anterior.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_modificar_datos)

        /* inicializar lista de avatares desde layout */
        imgOpciones = listOf(
            findViewById(R.id.imgVw_avatar2),
            findViewById(R.id.imgVw_avatar3),
            findViewById(R.id.imgVw_avatar4),
            findViewById(R.id.imgVw_avatar5),
            findViewById(R.id.imgVw_avatar6),
            findViewById(R.id.imgVw_avatar_defecto)
        )

        /* vincular vistas de entrada y botones */
        nombreActualizado = findViewById(R.id.editxtVw_contenidoCambioNombre)
        btnGuardarCambios = findViewById(R.id.btn_GuardarCambios)
        btnVolverDatosPersonales = findViewById(R.id.btn_VolverModificarDatos)

        /* verificar que haya un usuario logueado */
        if (usuarioId < 0) {
            finish()
            return
        }

        /* cargar datos actuales de usuario y preparar UI */
        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                ThreadlyDatabase.getDatabase(applicationContext)
                    .usuarioDAO()
                    .obtenerPorId(usuarioId)
            }

            if (usuario == null) {
                /* si no existe usuario en BD, cerrar actividad */
                finish()
                return@launch
            }

            /* mostrar nombre actual como hint */
            nombreActualizado.hint = usuario.username
            /* ajustar selección de avatar inicial */
            imagenSeleccionada = usuario.profilePic

            /* marcar todos los avatars con transparencia baja */
            imgOpciones.forEach { it.alpha = 0.5f }
            /* resaltar avatar seleccionado */
            if (imagenSeleccionada in 1..imgOpciones.size) {
                imgOpciones[imagenSeleccionada - 1].alpha = 1f
            }

            /* establecer listener para cada avatar: selecciona y resalta */
            imgOpciones.forEachIndexed { index, img ->
                img.alpha = 0.5f
                img.setOnClickListener {
                    imgOpciones.forEach { it.alpha = 0.5f }
                    img.alpha = 1f
                    imagenSeleccionada = index + 1
                }
            }

            /* configurar guardado de cambios */
            btnGuardarCambios.setOnClickListener {
                /* obtener nuevo nombre y cambiarlo si pasa la verificación */
                val nuevoNombre = nombreActualizado.text.toString().trim()
                val nombreFinal = if (nuevoNombre.isEmpty()) usuario.username else nuevoNombre

                /* verificar si hay cambios */
                if (nombreFinal == usuario.username && imagenSeleccionada == usuario.profilePic) {
                    Toast.makeText(
                        this@ModificarDatos,
                        "No has hecho ningún cambio",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                /* realizar actualización en BD */
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        ThreadlyDatabase.getDatabase(applicationContext)
                            .usuarioDAO()
                            .actualizar(
                                usuario.copy(
                                    username = nombreFinal,
                                    profilePic = imagenSeleccionada
                                )
                            )
                    }
                    /* notificar al usuario y cerrar pantalla */
                    Toast.makeText(
                        this@ModificarDatos,
                        "Datos actualizados correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }

        /* volver sin guardar cambios */
        btnVolverDatosPersonales.setOnClickListener {
            finish()
        }
    }
}
