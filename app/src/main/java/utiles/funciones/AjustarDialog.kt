package utiles.funciones

import android.app.Dialog
import android.content.res.Resources
import android.view.Gravity
import android.view.ViewGroup

/**
 * Ajusta las dimensiones y posición de un diálogo (`Dialog`) para que se muestre centrado
 * en la pantalla y con un ancho relativo al tamaño de la misma.
 *
 * Esta función es útil para mantener la consistencia visual en diferentes tamaños de pantalla
 * sin necesidad de definir dimensiones fijas en XML.
 *
 * @param dialog El diálogo a ajustar.
 * @param anchoPorcentaje El porcentaje del ancho de la pantalla que debe ocupar el diálogo (por defecto 0.85, es decir, 85%).
 *
 * ### Ejemplo de uso:
 * ```
 * val dialogo = Dialog(context)
 * ajustarDialog(dialogo)
 * dialogo.show()
 * ```
 */
fun ajustarDialog(dialog: Dialog, anchoPorcentaje: Double = 0.85) {
    /* se obtienen las dimensiones de la pantalla para que funcione en cualquier dispositivo */
    val dimensionPantalla = Resources.getSystem().displayMetrics

    /* cálculo del ancho del diálogo en función del porcentaje indicado */
    val anchoPantalla = dimensionPantalla.widthPixels
    val anchoDialog = (anchoPantalla * anchoPorcentaje).toInt()

    /* se aplica el ancho calculado, dejando que el alto se ajuste automáticamente */
    dialog.window?.setLayout(anchoDialog, ViewGroup.LayoutParams.WRAP_CONTENT)

    /* se centra el diálogo en pantalla */
    dialog.window?.setGravity(Gravity.CENTER)
}
