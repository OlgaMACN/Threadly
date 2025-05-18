package persistencia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import persistencia.dao.UsuarioDAO
import persistencia.entidades.Usuario

@Database(entities = [Usuario::class], version = 1)
abstract class ThreadlyBdD : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDAO
}