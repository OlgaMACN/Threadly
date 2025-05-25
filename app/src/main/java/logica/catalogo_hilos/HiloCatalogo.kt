package logica.catalogo_hilos

/**
 * Representa un hilo dentro del catálogo de la aplicación Threadly.
 * Contiene información básica como el número del hilo, su nombre identificativo
 * y un código de color (en formato hexadecimal).
 *
 * Esta clase se utiliza principalmente en la visualización, edición y almacenamiento
 * temporal del catálogo, así como en los adaptadores que gestionan la tabla de hilos.
 *
 * @property numHilo Número identificador del hilo, representado como cadena (puede incluir ceros iniciales).
 * @property nombreHilo Nombre o descripción asociada al hilo (por ejemplo, "Rojo Carmesí").
 * @property color Código de color en formato hexadecimal (por ejemplo, "#FF5733"), o null si no se especifica.
 */
data class HiloCatalogo(
    var numHilo: String,
    var nombreHilo: String,
    var color: String?
)
