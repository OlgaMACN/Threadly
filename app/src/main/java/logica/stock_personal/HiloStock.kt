package logica.stock_personal

/**
 * Representa un hilo dentro del inventario/stock  personal del usuario.
 * Contiene el identificador del hilo y la cantidad de madejas disponibles.
 *
 * Se utiliza en la gestión del stock para mostrar, actualizar y persistir
 * la cantidad de madejas de cada hilo que el usuario posee.
 *
 * @property hiloId Identificador único del hilo (por ejemplo, su código en el catálogo).
 * @property madejas Cantidad de madejas disponibles para este hilo.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
data class HiloStock(
    val hiloId: String,
    var madejas: Int
)
