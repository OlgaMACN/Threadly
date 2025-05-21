package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_hilos")
data class HiloStockEnt(
    @PrimaryKey val hiloId: String,
    var madejas: Int
)
