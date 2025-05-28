package persistencia.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import persistencia.daos.UsuarioDAO
import persistencia.entidades.GraficoEntidad
import persistencia.entidades.GraficoHilo
import persistencia.entidades.Hilo
import persistencia.entidades.Pedido
import persistencia.entidades.Stock
import persistencia.entidades.Usuario

/**
 * Clase que define la base de datos de Room para la aplicación Threadly.
 *
 * Esta base de datos contiene todas las entidades relacionadas con el sistema de gestión de hilos,
 * usuarios, gráficos, pedidos y el stock personal. Proporciona acceso a los DAOs que permiten
 * realizar operaciones de persistencia sobre dichas entidades.
 *
 * Se implementa como un singleton para asegurar que exista una única instancia
 * de la base de datos en toda la aplicación.
 *
 * @see RoomDatabase
 *
 * @author Olga y Sandra Macías Aragón
 */
@Database(
    entities = [
        Usuario::class,

    ],
    version = 2,
    exportSchema = false /* evita que Room genere archivos de esquema para esta base de datos */
)
abstract class ThreadlyDatabase : RoomDatabase() {

    /**
     * Devuelve el DAO para acceder a los datos de la entidad [Usuario].
     */
    abstract fun usuarioDAO(): UsuarioDAO


    companion object {
        @Volatile
        private var INSTANCE: ThreadlyDatabase? = null

        /**
         * Devuelve la instancia única de la base de datos [ThreadlyDatabase].
         * Si no existe, la crea utilizando el contexto de aplicación.
         *
         * @param context Contexto necesario para construir la base de datos.
         * @return Instancia de [ThreadlyDatabase].
         */
        fun getDatabase(context: Context): ThreadlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreadlyDatabase::class.java,
                    "threadly_database" /* nombre del archivo de la base de datos */
                )
                    .fallbackToDestructiveMigration(false) /* no borra datos si hay un cambio de versión */
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
