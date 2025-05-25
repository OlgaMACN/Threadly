package logica.pantalla_inicio

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.threadly.R
import logica.stock_personal.StockSingleton
import utiles.BaseActivity
import utiles.funciones.funcionToolbar

/**
 * Pantalla principal de bienvenida de la aplicación Threadly.
 * Muestra el nombre del usuario, la cantidad de madejas en stock,
 * un consejo aleatorio y permite navegar a la configuración personal.
 *
 * Esta clase extiende de [BaseActivity] para aprovechar la funcionalidad común.
 *
 * @ author Olga y Sandra Macías Aragón
 */
class PantallaPrincipal : BaseActivity() {

    /**
     * Se ejecuta al crear la actividad. Inicializa el toolbar, carga el usuario,
     * muestra el stock actual, un consejo aleatorio y permite ir a la pantalla de configuración.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)

        funcionToolbar(this) /* carga el toolbar personalizado */

        cargarUsuario() /* muestra imagen y nombre del usuario */

        /* inicializa el stock si es necesario y muestra el total de madejas */
        StockSingleton.inicializarStockSiNecesario(this)
        val totalMadejas = StockSingleton.mostrarTotalStock()

        val txtStock = findViewById<TextView>(R.id.txtVw_contenidoStock)
        txtStock.text = "$totalMadejas"

        /* muestra el nombre del usuario registrado (por seguridad si cargó antes de tiempo) */
        findViewById<TextView>(R.id.txtVw_nombreUsuario).text = nombreUsuario

        /* muestra un consejo aleatorio en la parte inferior de la pantalla */
        val txtTip = findViewById<TextView>(R.id.txtVw_contenidoTip)
        txtTip.text = obtenerConsejoAleatorio()

        /* abre la pantalla de configuración (datos personales) */
        findViewById<ImageButton>(R.id.imgBtn_configuracion).setOnClickListener {
            irAActividad(DatosPersonales::class.java)
        }
    }

    /**
     * Método que se ejecuta cada vez que la pantalla vuelve a estar visible.
     * Refresca los datos del usuario y el stock en pantalla.
     */
    override fun onResume() {
        super.onResume()
        cargarUsuario()

        StockSingleton.inicializarStockSiNecesario(this)
        val total = StockSingleton.mostrarTotalStock()
        findViewById<TextView>(R.id.txtVw_contenidoStock).text = "$total"
    }

    /**
     * Carga el usuario en memoria (imagen y nombre) y actualiza la interfaz.
     * Si no hay usuario cargado, no realiza ninguna acción.
     */
    private fun cargarUsuario() {
        val usuario = DatosPersonales.usuarioEnMemoria
        usuario?.let {
            /* asigna la imagen de perfil según el id guardado */
            findViewById<ImageView>(R.id.imgVw_imagenPerfil).setImageResource(
                when (it.idImagen) {
                    1 -> R.drawable.img_avatar2
                    2 -> R.drawable.img_avatar3
                    3 -> R.drawable.img_avatar4
                    4 -> R.drawable.img_avatar5
                    5 -> R.drawable.img_avatar6
                    6 -> R.drawable.img_avatar_defecto
                    else -> R.drawable.img_avatar_defecto
                }
            )
            /* muestra el nombre del usuario en pantalla */
            findViewById<TextView>(R.id.txtVw_nombreUsuario).text = it.nombre
        }
    }

    /**
     * Devuelve un consejo aleatorio para bordado y organización.
     * Estos consejos son fijos y no están persistidos en la base de datos.
     *
     * @return Un [String] con el consejo elegido aleatoriamente.
     */
    private fun obtenerConsejoAleatorio(): String {
        val consejos = listOf(
            "Organiza tus hilos por colores.",
            "Etiqueta tus madejas para no perder el número.",
            "Guarda tus gráficos en carpetas por dificultad.",
            "Usa una luz blanca al bordar de noche.",
            "Haz pausas para evitar cansancio visual.",
            "Consejo no disponible"
        )
        return consejos.random()
    }
}
