package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Hilo
import persistencia.entidades.Stock

data class StockConHilo(
    @Embedded val stock: Stock,

    @Relation(
        parentColumn = "threadId",
        entityColumn = "threadId"
    )
    val hilo: Hilo
)
