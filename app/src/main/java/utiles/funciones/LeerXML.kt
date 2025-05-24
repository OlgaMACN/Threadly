package utiles.funciones

import android.content.Context
import logica.catalogo_hilos.HiloCatalogo
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

fun leerXML(context: Context, resourceId: Int): List<HiloCatalogo> {
    val listaHilos = mutableListOf<HiloCatalogo>()

    context.resources.openRawResource(resourceId).use { inputStream ->
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(inputStream)
        val nodosHilo = doc.getElementsByTagName("hilo")

        for (i in 0 until nodosHilo.length) {
            val nodo = nodosHilo.item(i) as Element
            val codigo = nodo.getElementsByTagName("codigo").item(0).textContent.trim()
            val nombre = nodo.getElementsByTagName("nombre").item(0).textContent.trim()
            val color = nodo.getElementsByTagName("color").item(0).textContent.trim()

            listaHilos.add(
                HiloCatalogo(
                    numHilo = codigo,
                    nombreHilo = nombre,
                    color = color.takeIf { it.isNotEmpty() }  /* si está vacío. será null */
                )
            )
        }
    }
    return listaHilos
}
