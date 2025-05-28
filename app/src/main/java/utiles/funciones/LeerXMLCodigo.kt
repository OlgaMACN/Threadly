package utiles.funciones

import android.content.Context
import logica.stock_personal.HiloStock
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Lee un archivo XML y extrae únicamente los códigos de hilo,
 * generando una lista de objetos [HiloStock] con cantidad inicial 0.
 *
 * Esta función está pensada para importar un conjunto de códigos de hilos,
 * por ejemplo, para inicializar un stock vacío en base a un archivo de catálogo.
 *
 * @param context El contexto de la aplicación, necesario para acceder a los recursos.
 * @param resourceId El ID del recurso XML ubicado en la carpeta `res/raw`.
 * @return Una lista mutable de [HiloStock] con los códigos leídos y cantidad 0.
 *
 * ### Estructura esperada del XML:
 * ```xml
 * <catalogo>
 *   <hilo>
 *     <codigo>310</codigo>
 *   </hilo>
 *   ...
 * </catalogo>
 * ```
 *
 * - Sólo se utiliza el contenido de la etiqueta `<codigo>`.
 * - Los códigos se transforman a mayúsculas automáticamente.
 *
 * * @author Olga y Sandra Macías Aragón
 */
fun LeerXMLCodigo(context: Context, resourceId: Int): MutableList<HiloStock> {
    val lista = mutableListOf<HiloStock>()
    context.resources.openRawResource(resourceId).use { inputStream ->
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(inputStream)
        val nodosHilo = doc.getElementsByTagName("codigo")

        for (i in 0 until nodosHilo.length) {
            val nodo = nodosHilo.item(i) as Element
            val codigo = nodo.textContent.trim().uppercase()
            lista.add(HiloStock(codigo, 0))
        }
        return lista
    }
}
