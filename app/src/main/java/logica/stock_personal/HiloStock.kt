package logica.stock_personal

/**
 * Representa un hilo dentro del inventario personal del usuario.
 * Esta clase se utiliza para mostrar, modificar y persistir la cantidad de madejas
 * que posee el usuario para un hilo determinado.
 *
 * Es una clase de datos (data class), lo que significa que proporciona automáticamente
 * funcionalidades útiles como `equals()`, `hashCode()` y `toString()`.
 *
 * @property hiloId Identificador único del hilo (por ejemplo, su código en el catálogo).
 * @property madejas Número de madejas disponibles del hilo.
 *
 * * @author Olga y Sandra Macías Aragón
 */
data class HiloStock(
    val hiloId: String,
    var madejas: Int
)
