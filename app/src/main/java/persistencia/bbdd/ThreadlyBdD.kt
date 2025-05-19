package persistencia.bbdd

import androidx.room.Database
import androidx.room.RoomDatabase
import persistencia.dao.ConsejoDAO
import persistencia.dao.UsuarioDAO
import persistencia.entidades.Consejo
import persistencia.entidades.Usuario

/* hay que agregar todas las entidades que vayan a utilizarse */
@Database(entities = [Usuario::class, Consejo::class], version = 1)
abstract class ThreadlyBdD : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDAO
    abstract fun consejoDao(): ConsejoDAO


}