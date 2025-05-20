package persistencia.bbdd

import Catalogo
import CatalogoDAO
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Catalogo::class], version = 1)
abstract class CatalogoBdD : RoomDatabase() {
    abstract fun hiloDao(): CatalogoDAO

    companion object {
        @Volatile
        private var INSTANCE: CatalogoBdD? = null

        fun getDatabase(context: Context): CatalogoBdD {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CatalogoBdD::class.java,
                    "hilos_catalogo_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

