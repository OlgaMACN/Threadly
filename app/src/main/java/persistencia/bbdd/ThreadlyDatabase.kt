package persistencia.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import persistencia.daos.HiloCatalogoDao
import persistencia.daos.UsuarioDAO
import persistencia.entidades.HiloCatalogoEntity
import persistencia.entidades.Usuario
import utiles.SesionUsuario
import com.threadly.R


// TODO cambiar todo esto a producción, ahora está en desarrollo hasta que termine de añadir entidades
@Database(
    entities = [
        Usuario::class, HiloCatalogoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ThreadlyDatabase : RoomDatabase() {

    abstract fun usuarioDAO(): UsuarioDAO
    abstract fun hiloCatalogoDao(): HiloCatalogoDao

    companion object {
        @Volatile
        private var INSTANCE: ThreadlyDatabase? = null

        fun getDatabase(context: Context): ThreadlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreadlyDatabase::class.java,
                    "threadly_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
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
