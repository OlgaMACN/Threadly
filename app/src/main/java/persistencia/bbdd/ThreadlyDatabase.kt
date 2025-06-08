package persistencia.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import persistencia.daos.GraficoDao
import persistencia.daos.HiloCatalogoDao
import persistencia.daos.HiloGraficoDao
import persistencia.daos.HiloStockDao
import persistencia.daos.PedidoDao
import persistencia.daos.UsuarioDAO
import persistencia.entidades.GraficoEntity
import persistencia.entidades.HiloCatalogoEntity
import persistencia.entidades.HiloGraficoEntity
import persistencia.entidades.HiloStockEntity
import persistencia.entidades.PedidoEntity
import persistencia.entidades.Usuario
import utiles.SesionUsuario

/**
 * Base de datos principal de la aplicación Threadly.
 *
 * Define todas las entidades y DAOs necesarios para gestionar:
 * - Usuarios registrados
 * - Catálogo de hilos por usuario
 * - Stock personal de madejas
 * - Pedidos almacenados
 * - Gráficos asociados a pedidos
 * - Hilos asignados a cada gráfico
 *
 * @version 19 (fase de desarrollo, aún no estable para producción)
 * @author Olga y Sandra Macías Aragón
 *
 * TODO IMPORTANTE: Esta versión de la base de datos está en desarrollo y usa `fallbackToDestructiveMigration()`,
 * lo que implica que se borrarán los datos con cada cambio de versión.
 */
@Database(
    entities = [
        Usuario::class,
        HiloCatalogoEntity::class,
        HiloStockEntity::class,
        HiloGraficoEntity::class,
        GraficoEntity::class,
        PedidoEntity::class
    ],
    version = 19,
    exportSchema = false
)
abstract class ThreadlyDatabase : RoomDatabase() {

    /* DAOs disponibles */
    abstract fun usuarioDAO(): UsuarioDAO
    abstract fun hiloCatalogoDao(): HiloCatalogoDao
    abstract fun hiloStockDao(): HiloStockDao
    abstract fun hiloGraficoDao(): HiloGraficoDao
    abstract fun graficoDao(): GraficoDao
    abstract fun pedidoDao(): PedidoDao

    /* las clases que hacen de relación no se introducen en la BdD */

    companion object {
        @Volatile
        private var INSTANCE: ThreadlyDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos Threadly.
         * Si no existe, la crea y lanza una callback para insertar un usuario de prueba.
         *
         * Esta función usa `fallbackToDestructiveMigration()` para reiniciar la base de datos
         * en cada cambio de versión mientras dure el desarrollo.
         */
        fun getDatabase(context: Context): ThreadlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreadlyDatabase::class.java,
                    "threadly_database"
                )
                    .fallbackToDestructiveMigration() /* IMPORTANTE: reinicia datos al cambiar la versión */
                    .addCallback(object : Callback() {

                        /**
                         * Callback que se ejecuta al crear por primera vez la base de datos.
                         *
                         * Crea automáticamente un usuario de prueba ("prueba"/"1234") con avatar por defecto
                         * y lo inicia como sesión actual.
                         */
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).usuarioDAO()
                                val usuarios = dao.obtenerTodos()

                                if (usuarios.isEmpty()) {
                                    val usuarioPrueba = Usuario(
                                        username = "prueba",
                                        password = "1234",
                                        profilePic = R.drawable.img_avatar_defecto
                                    )
                                    val id = dao.insertar(usuarioPrueba).toInt()
                                    SesionUsuario.guardarSesion(context, id)
                                }
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
