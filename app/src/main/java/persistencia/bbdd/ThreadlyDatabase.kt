package persistencia.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
