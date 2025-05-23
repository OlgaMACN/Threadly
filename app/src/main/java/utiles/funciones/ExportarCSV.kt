package utiles.funciones

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import logica.almacen_pedidos.PedidoGuardado

@RequiresApi(Build.VERSION_CODES.Q)
fun exportarPedidoCSV(context: Context, pedido: PedidoGuardado): Boolean {
    val resolver = context.contentResolver /* para poder acceder a las carpetas de android */
    val nombreArchivo = "${pedido.nombre}.csv" /* nombre del archivo */

    /* metadatos del archivo (nombre, tipo MIME, carpeta destino) */
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv") /* mime = tipo de contenido (csv) */
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Threadly")
    }

    /* se inserta el archivo en MediaStore */
    val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        ?: return false

    /* mapea de todos los hilos del pedido a un mapa (código -> cantidad total) */
    val mapaHilos = mutableMapOf<String, Int>()
    for (grafico in pedido.graficos) {
        for (hilo in grafico.listaHilos) {
            val codigo = hilo.hilo
            val cantidad = hilo.madejas
            mapaHilos[codigo] = mapaHilos.getOrDefault(codigo, 0) + cantidad
        }
    }

    /* se escribe el archivo CSV, con sus cabeceras */
    try {
        resolver.openOutputStream(itemUri)?.use { outputStream ->
            outputStream.writer().use { writer ->
                writer.append("Hilo,Madejas\n")
                for ((codigo, total) in mapaHilos) {
                    writer.append("$codigo,$total\n")
                }
                writer.flush()
            }
        }
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

