package logica.catalogo_hilos

/**
 * Representa un elemento de catálogo de hilos en la aplicación Threadly.
 *
 * Contiene la información esencial para visualizar y manipular un hilo:
 *
 * @property numHilo Identificador del hilo, formato texto (puede incluir ceros iniciales).
 * @property nombreHilo Nombre o descripción del hilo (por ejemplo, "Rojo Carmesí").
 * @property color Código hexadecimal de color asociado al hilo (por ejemplo, "#FF5733"),
 *                  o null si no se especifica color.
 *
 * Utilizado en:
 *  - Adaptadores de RecyclerView para mostrar filas de catálogo.
 *  - Diálogos de creación y modificación de hilos.
 *  - Lógica de búsqueda y resaltado en pantalla.
 *
 * @constructor Crea una instancia de [HiloCatalogo] con los atributos indicados.
 *
 * @param numHilo Número identificador del hilo.
 * @param nombreHilo Nombre del hilo.
 * @param color Color del hilo gracias al parseo del adaptador.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
data class HiloCatalogo(
    var numHilo: String,
    var nombreHilo: String,
    var color: String?
)
