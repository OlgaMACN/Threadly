package utiles.funciones

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import modelo.PedidoGuardado


/**
 * Exporta un pedido a un archivo CSV en la carpeta de descargas del dispositivo,
 * dentro de un subdirectorio llamado "Threadly".
 *
 * Esta función está disponible solo para Android 10 (API 29) o superior,
 * ya que usa `MediaStore.Downloads` y escritura en almacenamiento externo gestionado.
 *
 * El archivo CSV generado contiene la lista de hilos y el número total de madejas necesarias,
 * combinando los datos de todos los gráficos del pedido.
 *
 * @param context Contexto de la aplicación necesario para acceder al `ContentResolver`.
 * @param pedido Objeto [PedidoGuardado] que contiene los gráficos y sus hilos.
 * @return `true` si el archivo se exportó correctamente, `false` en caso de error.
 *
 * ### Formato del archivo generado:
 * ```
 * Hilo,Madejas
 * 310,2
 * 321,5
 * ...
 * ```
 * * @author Olga y Sandra Macías Aragón
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun exportarPedidoCSV(context: Context, pedido: PedidoGuardado): Boolean {
    val resolver = context.contentResolver /* para acceder a las carpetas del dispositivo */
    val nombreArchivo = "${pedido.nombre}.csv"

    /* metadatos del archivo: nombre, tipo de contenido (CSV), y carpeta destino */
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Threadly")
    }

    /* se inserta el archivo en el sistema de almacenamiento gestionado por Android */
    val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        ?: return false

    /* se crea un mapa de hilos (clave: código del hilo, valor: cantidad total de madejas) */
    val mapaHilos = mutableMapOf<String, Int>()
    for (grafico in pedido.graficos) {
        for (hilo in grafico.listaHilos) {
            val codigo = hilo.hilo
            val cantidad = hilo.madejas
            mapaHilos[codigo] = mapaHilos.getOrDefault(codigo, 0) + cantidad
        }
    }

    /* escritura del contenido CSV en el archivo */
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
