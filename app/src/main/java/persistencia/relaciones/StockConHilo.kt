package persistencia.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import persistencia.entidades.Hilo
import persistencia.entidades.Stock

/**
 * Representa la relación entre un objeto [Stock] y su correspondiente [Hilo].
 *
 * Esta clase permite obtener un stock específico junto con la información completa
 * del hilo asociado a dicho stock.
 *
 * @property stock El objeto [Stock] embebido.
 * @property hilo El objeto [Hilo] relacionado a este stock, basado en el campo común `threadId`.
 */
data class StockConHilo(
    @Embedded val stock: Stock,

    @Relation(
        parentColumn = "threadId",
        entityColumn = "threadId"
    )
    val hilo: Hilo
)
