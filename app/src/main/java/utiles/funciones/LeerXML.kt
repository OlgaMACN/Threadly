package utiles.funciones

import android.content.Context
import logica.catalogo_hilos.HiloCatalogo
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Lee y convierte un archivo XML en una lista de objetos [HiloCatalogo].
 *
 * Esta función parsea un archivo XML que contiene elementos `<hilo>` con las etiquetas
 * internas `<codigo>`, `<nombre>` y `<color>`, generando una lista de hilos que
 * representan el catálogo base de la aplicación.
 *
 * @param context El contexto de la aplicación para acceder a los recursos.
 * @param resourceId El ID del recurso XML ubicado en la carpeta `res/raw`.
 * @return Una lista de objetos [HiloCatalogo] extraídos del XML.
 *
 * ### Estructura esperada del XML:
 * ```xml
 * <catalogo>
 *   <hilo>
 *     <codigo>310</codigo>
 *     <nombre>Negro</nombre>
 *     <color>#000000</color>
 *   </hilo>
 *   ...
 * </catalogo>
 * ```
 *
 * - Si la etiqueta `<color>` está vacía, se interpretará como `null`.
 */
fun leerXML(context: Context, resourceId: Int): List<HiloCatalogo> {
    val listaHilos = mutableListOf<HiloCatalogo>()

    /* abre el archivo XML del recurso especificado */
    context.resources.openRawResource(resourceId).use { inputStream ->
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(inputStream)
        val nodosHilo = doc.getElementsByTagName("hilo")

        /* itera sobre cada nodo <hilo> y extrae la información necesaria */
        for (i in 0 until nodosHilo.length) {
            val nodo = nodosHilo.item(i) as Element
            val codigo = nodo.getElementsByTagName("codigo").item(0).textContent.trim()
            val nombre = nodo.getElementsByTagName("nombre").item(0).textContent.trim()
            val color = nodo.getElementsByTagName("color").item(0).textContent.trim()

            listaHilos.add(
                HiloCatalogo(
                    numHilo = codigo,
                    nombreHilo = nombre,
                    color = color.takeIf { it.isNotEmpty() } /* si está vacío será null */
                )
            )
        }
    }
    return listaHilos
}
