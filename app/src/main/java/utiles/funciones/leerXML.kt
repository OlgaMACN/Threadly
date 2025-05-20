package utiles.funciones

import persistencia.entidades.Catalogo
import android.content.Context
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/* leer el xml con dom para crear la BdD */
fun leerXML(context: Context, resourceId: Int): List<Catalogo> {
    val listaHilos = mutableListOf<Catalogo>()

    context.resources.openRawResource(resourceId).use { inputStream ->
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(inputStream)
        val nodosHilo = doc.getElementsByTagName("Hilo")

        for (i in 0 until nodosHilo.length) {
            val nodo = nodosHilo.item(i) as Element
            val codigo = nodo.getElementsByTagName("Codigo").item(0).textContent.trim()
            val nombre = nodo.getElementsByTagName("Nombre").item(0).textContent.trim()
            val color = nodo.getElementsByTagName("Color").item(0).textContent.trim()

            listaHilos.add(
                Catalogo(
                    id = 0,  /* se autogenera */
                    codigoHilo = codigo,
                    nombreHilo = nombre,
                    color = color
                )
            )
        }
    }
    return listaHilos
}
