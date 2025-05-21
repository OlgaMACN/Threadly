package persistencia.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import persistencia.entidades.HiloStockEnt
import persistencia.dao.HiloStockDAO

@Database(entities = [HiloStockEnt::class], version = 1)
abstract class StockBdD : RoomDatabase() {
    abstract fun hiloStockDao(): HiloStockDAO

    companion object {
        @Volatile
        private var INSTANCE: StockBdD? = null

        fun getDatabase(context: Context): StockBdD {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockBdD::class.java,
                    "stock_hilos_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
