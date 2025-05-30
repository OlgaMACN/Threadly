package logica.pantalla_inicio

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlyDatabase
import utiles.BaseActivity
import utiles.Consejos
import utiles.SesionUsuario
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

    private lateinit var txtTip: TextView
    private lateinit var txtNombreUser: TextView
    private lateinit var imgPerfil: ImageView

    /**
     * Se ejecuta al crear la actividad. Inicializa el toolbar, carga el usuario,
     * muestra el stock actual, un consejo aleatorio y permite ir a la pantalla de configuración.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)

        funcionToolbar(this) /* carga el toolbar personalizado */

        cargarUsuario() /* muestra imagen y nombre del usuario */
        txtNombreUser = findViewById(R.id.txtVw_nombreUsuario)
        imgPerfil = findViewById(R.id.imgVw_imagenPerfil)
        txtTip = findViewById(R.id.txtVw_contenidoTip)

        /* inicializa el stock si es necesario y muestra el total de madejas */
      /*  StockSingleton.inicializarStockSiNecesario(this)
        val totalMadejas = StockSingleton.mostrarTotalStock()

        val txtStock = findViewById<TextView>(R.id.txtVw_contenidoStock)
        txtStock.text = "$totalMadejas"*/

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
        /* cartelito stock */
       /* StockSingleton.inicializarStockSiNecesario(this)
        val total = StockSingleton.mostrarTotalStock()
        findViewById<TextView>(R.id.txtVw_contenidoStock).text = "$total"*/
        /* cartelito consejo */
        consejoAleatorio()
        /* cargar el usuario logueado */
        cargarUsuario()

    }

    /**
     * Carga el usuario en memoria (imagen y nombre) y actualiza la interfaz.
     * Si no hay usuario cargado, no realiza ninguna acción.
     */
    private fun cargarUsuario() {
        val userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) return

        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                ThreadlyDatabase.getDatabase(applicationContext)
                    .usuarioDAO()
                    .obtenerPorId(userId)
            }
            usuario?.let {
                txtNombreUser.text = it.username
                imgPerfil.setImageResource(
                    when (it.profilePic) {
                        1 -> R.drawable.img_avatar2
                        2 -> R.drawable.img_avatar3
                        3 -> R.drawable.img_avatar4
                        4 -> R.drawable.img_avatar5
                        5 -> R.drawable.img_avatar6
                        else -> R.drawable.img_avatar_defecto
                    }
                )
            }
        }
    }

    /**
     * Devuelve un consejo aleatorio para bordado y organización.
     * Estos consejos son fijos y no están persistidos en la base de datos.
     *
     * @return Un [String] con el consejo elegido aleatoriamente.
     */
    private fun consejoAleatorio() {
        val consejo = Consejos.obtenerAleatorio()
        this.txtTip.text = consejo
    }

}
