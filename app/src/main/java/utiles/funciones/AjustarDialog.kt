package utiles.funciones;

import android.app.Dialog
import android.content.res.Resources
import android.view.Gravity
import android.view.ViewGroup

/* centrar los dialogs en la pantalla */
fun ajustarDialog(dialog: Dialog, anchoPorcentaje: Double = 0.85) {
    /* primero se obtienen las dimensiones de la pantalla (para que sirva en cualquier dispositivo) */
    val dimensionPantalla = Resources.getSystem().displayMetrics

    /* ancho calculado, el alto no porque se corta */
    val anchoPantalla = dimensionPantalla.widthPixels
    val anchoDialog = (anchoPantalla * anchoPorcentaje).toInt()

    /* gracias a setLayout se ajusta según los parámetros configurados en el paso anterior */
    dialog.window?.setLayout(anchoDialog, ViewGroup.LayoutParams.WRAP_CONTENT)

    /* y se centra en pantalla */
    dialog.window?.setGravity(Gravity.CENTER)
}
