package utiles.funciones

import android.content.Context
import logica.stock_personal.HiloStock
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory


/* leer sólo el nodo codigo para el stock */
fun leerCodigoHilo(context: Context, resourceId: Int): MutableList<HiloStock> {
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

