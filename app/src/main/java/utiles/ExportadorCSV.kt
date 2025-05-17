package utiles

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* Patrón Singleton porque no necesita instanciarse (thread-safe) y tampoco va a cambiar */
object ExportadorCSV {

    /**
     * Exporta un mapa de datos (clave=Hilo, valor=Madejas) a un archivo CSV
     * Se guarda en la carpeta Descargas, usando MediaStore (Android 10+) o acceso directo (API < 29)
     */
    fun exportarPedido(context: Context, datos: Map<String, Int>) {
        if (datos.isEmpty()) {
            Toast.makeText(context, "Aún hay madejas en el pedido", Toast.LENGTH_SHORT).show()
            return
        }

        // Generamos un nombre con fecha/hora
        val ahora = Date()
        val formatoNombre = SimpleDateFormat("dd-MM-yy_HH-mm", Locale.getDefault())
        val nombreArchivo = "P_${formatoNombre.format(ahora)}.csv"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 🔒 Android 10 en adelante: MediaStore (sin permisos)
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.writer().use { writer ->
                            writer.append("Hilo,Madejas\n")
                            for ((hilo, total) in datos) {
                                writer.append("\"$hilo\",$total\n")
                            }
                            writer.flush()
                        }
                    }

                    // Marcar como finalizado
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                    Toast.makeText(context, "Pedido guardado en Descargas", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(context, "No se pudo crear el archivo.", Toast.LENGTH_LONG)
                        .show()
                }

            } else {
                // 📂 Android 9 o anterior: escribir directamente en /Download
                val carpetaDescargas =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!carpetaDescargas.exists()) carpetaDescargas.mkdirs()

                val archivo = File(carpetaDescargas, nombreArchivo)

                FileWriter(archivo).use { writer ->
                    writer.append("Hilo,Madejas\n")
                    for ((hilo, total) in datos) {
                        writer.append("\"$hilo\",$total\n")
                    }
                    writer.flush()
                }

                Toast.makeText(
                    context,
                    "Pedido guardado en: ${archivo.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al guardar el pedido: ${e.message}", Toast.LENGTH_LONG)
                .show()
            e.printStackTrace()
        }
    }
}
