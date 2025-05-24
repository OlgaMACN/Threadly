package persistencia.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import persistencia.daos.GraficoDAO
import persistencia.daos.GraficoHiloDAO
import persistencia.daos.HiloDAO
import persistencia.daos.PedidoDAO
import persistencia.daos.StockDAO
import persistencia.daos.UsuarioDAO
import persistencia.entidades.Grafico
import persistencia.entidades.GraficoHilo
import persistencia.entidades.Hilo
import persistencia.entidades.Pedido
import persistencia.entidades.Stock
import persistencia.entidades.Usuario


/**
 * Clase que representa la base de datos principal de Threadly.
 * Aquí se definen las entidades y sus DAOs correspondientes.
 */
@Database(
    entities = [
        Usuario::class,
        Hilo::class,
        Stock::class,
        Pedido::class,
        Grafico::class,
        GraficoHilo::class,
    ],
    version = 1,
    exportSchema = false /* evita que Room genere archivos de esquema */
)
abstract class ThreadlyDatabase : RoomDatabase() {

    abstract fun usuarioDAO(): UsuarioDAO
    abstract fun hiloDAO(): HiloDAO
    abstract fun stockDAO(): StockDAO
    abstract fun pedidoDAO(): PedidoDAO
    abstract fun graficoDAO(): GraficoDAO
    abstract fun graficoHiloDAO(): GraficoHiloDAO

    companion object {
        @Volatile
        private var INSTANCE: ThreadlyDatabase? = null

        /**
         * Obtiene una instancia única de la base de datos.
         */
        fun getDatabase(context: Context): ThreadlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreadlyDatabase::class.java,
                    "threadly_database"
                ).fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}