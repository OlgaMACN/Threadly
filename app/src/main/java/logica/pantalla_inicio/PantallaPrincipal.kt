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
import persistencia.daos.GraficoDao
import persistencia.daos.HiloStockDao
import persistencia.daos.PedidoDao
import utiles.BaseActivity
import utiles.Consejos
import utiles.PrecargaDatos.precargarCatalogoYStockSiNoExisten
import utiles.SesionUsuario
import utiles.funciones.funcionToolbar

/**
 * Actividad principal de la aplicación, donde se muestra la información general del usuario,
 * estado de sus pedidos y stock, además de ofrecer un consejo aleatorio.
 *
 * Esta clase extiende de [BaseActivity] para incorporar funcionalidades
 * comunes como la navegación y configuración de toolbar, o el registro de la sesión activa.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
class PantallaPrincipal : BaseActivity() {

    /**
     * Vista que muestra el texto del consejo aleatorio
     */
    private lateinit var txtTip: TextView

    /**
     * Vista que muestra el nombre de usuario actualmente en sesión
     */
    private lateinit var txtNombreUser: TextView

    /**
     * Vista que muestra la imagen de perfil del usuario
     */
    private lateinit var imgPerfil: ImageView

    /**
     * DAO para acceder a la tabla de stock de madejas por usuario
     */
    private lateinit var dao: HiloStockDao

    /**
     * DAO para acceder a la tabla de pedidos del usuario
     */
    private lateinit var pedidoDao: PedidoDao

    /**
     * DAO para acceder a datos de gráficos y pedidos en curso
     */
    private lateinit var graficoDao: GraficoDao

    /**
     * Vista que muestra la cantidad total de madejas en stock
     */
    private lateinit var txtStock: TextView

    /**
     * Identificador del usuario actualmente logueado.
     * Se inicializa a -1 para indicar si hay ausencia de usuario
     */
    private var userId: Int = -1

    /**
     * Punto de partida de la actividad
     *
     * - Asigna el layout de la pantalla.
     * - Configura el toolbar personalizado.
     * - Obtiene las referencias a las vistas (TextViews, ImageView, etc).
     * - Inicializa DAOs para las operaciones de la base de datos.
     * - Comprueba la sesión de usuario y finaliza si no hay sesión.
     * - Lanza una corrutina para precargar catálogo y stock si no existen.
     * - Carga datos iniciales del usuario (nombre e imagen) y configura la
     *   navegación a la pantalla de configuración.
     *
     * @param savedInstanceState Estado previo de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)
        funcionToolbar(this) /* carga el toolbar personalizado desde utilidades */

        /* inicialización de vistas desde el layout */
        txtNombreUser = findViewById(R.id.txtVw_nombreUsuario)
        imgPerfil = findViewById(R.id.imgVw_imagenPerfil)
        txtTip = findViewById(R.id.txtVw_contenidoTip)
        txtStock = findViewById(R.id.txtVw_contenidoStock)

        /* inicialización de DAOs de acceso a datos */
        dao = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        pedidoDao = ThreadlyDatabase.getDatabase(applicationContext).pedidoDao()
        graficoDao = ThreadlyDatabase.getDatabase(applicationContext).graficoDao()

        /* obtención del identificador de usuario de la sesión actual */
        userId = SesionUsuario.obtenerSesion(this)
        /* si no hay sesión válida, finaliza la actividad para evitar errores */
        if (userId < 0) {
            finish()
            return
        }

        /* precarga asíncrona del catálogo y stock si no existen para el usuario */
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                /* método utilitario que inserta datos iniciales en BD si faltan */
                precargarCatalogoYStockSiNoExisten(applicationContext, userId)
            }
            /* tras finalizar precarga, onResume se encargará de refrescar datos */
        }

        /* primera carga de datos del usuario en UI (nombre e imagen) */
        cargarUsuario()

        /* botón de configuración para abrir DatosPersonales */
        findViewById<ImageButton>(R.id.imgBtn_configuracion).setOnClickListener {
            irAActividad(DatosPersonales::class.java)
        }
    }

    /**
     * Llamado cuando la actividad vuelve a primer plano.
     *
     * - Refresca el valor total de stock de madejas.
     * - Muestra el nombre del gráfico más reciente en curso o mensaje por defecto.
     * - Actualiza el consejo aleatorio y recarga datos de usuario.
     */
    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            /* calcular total de madejas en stock desde la BD */
            val totalMadejas = withContext(Dispatchers.IO) {
                dao.obtenerStockPorUsuario(userId)
                    .sumOf { it.madejas }
            }

            /* mostrar total de madejas en la vista correspondiente */
            txtStock.text = "$totalMadejas"

            /* obtener y mostrar el nombre del último gráfico/pedido en curso */
            val txtGrafico = findViewById<TextView>(R.id.txtVw_contenidoGrafico)
            val ultimoGrafico = withContext(Dispatchers.IO) {
                graficoDao.obtenerUltimoGraficoEnCurso(userId)
            }

            txtGrafico.text = if (ultimoGrafico != null) {
                ultimoGrafico.nombre
            } else {
                /* mensaje por defecto cuando no hay pedidos en curso */
                "Sin pedidos en curso"
            }

            /* actualizar consejo aleatorio y refrescar los datos del usuario */
            consejoAleatorio()
            cargarUsuario()
        }
    }

    /**
     * Carga en la interfaz el nombre de usuario y la imagen de perfil.
     *
     * - Obtiene la sesión activa, sale si no hay usuario.
     * - Recupera datos de la BdD en corrutina para no crashear la app.
     * - Asigna texto y recurso de imagen a las vistas.
     *
     * @see SesionUsuario
     * @see ThreadlyDatabase.usuarioDAO
     */
    private fun cargarUsuario() {
        /* obtener ID de sesión y terminar si inválido */
        val id = SesionUsuario.obtenerSesion(this).takeIf { it >= 0 } ?: return
        lifecycleScope.launch {
            /* acceso a BD en hilo de IO para recuperar usuario */
            val u = withContext(Dispatchers.IO) {
                ThreadlyDatabase.getDatabase(applicationContext)
                    .usuarioDAO()
                    .obtenerPorId(id)
            } ?: return@launch /* si usuario no existe, adiós */

            /* asignación de nombre de usuario en la interfaz */
            txtNombreUser.text = u.username
            /* selección de imagen de perfil según valor almacenado */
            imgPerfil.setImageResource(
                when (u.profilePic) {
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

    /**
     * Selecciona y muestra un consejo aleatorio.
     *
     * Los consejos están definidos en [Consejos] y no
     * se almacenan en la base de datos para no consumir
     * recursos innecesarios.
     *
     * @see Consejos.obtenerAleatorio
     */
    private fun consejoAleatorio() {
        /* obtener cadena de texto de consejo aleatorio y mostrarla */
        val consejo: String = Consejos.obtenerAleatorio()
        this.txtTip.text = consejo
    }
}
