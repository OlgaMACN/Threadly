package toolbar

import CatalogoHilos.CatalogoHilos
import Foro.Foro
import PantallaInicio.PantallaPrincipal
import android.app.Activity
import android.content.Intent
import android.widget.ImageButton
import com.threadly.R
import pedido_hilos.PedidoHilos
import stock_personal.StockPersonal

fun funcionToolbar(activity: Activity) {
    /* inicialización de botones del toolbar */
    val btn_inicio = activity.findViewById<ImageButton>(R.id.botonInicio)
    val btn_catalogo = activity.findViewById<ImageButton>(R.id.botonCatalogo)
    val btn_stock = activity.findViewById<ImageButton>(R.id.botonStock)
    val btn_foro = activity.findViewById<ImageButton>(R.id.botonForo)
    val btn_pedido = activity.findViewById<ImageButton>(R.id.botonPedido)

    /* configuración botón inicio */
    btn_inicio.setOnClickListener() {
        activity.startActivity(Intent(activity, PantallaPrincipal::class.java))
    }

    /* configuración botón catálogo */
    btn_catalogo.setOnClickListener() {
        activity.startActivity(Intent(activity, CatalogoHilos::class.java))
    }

    /* configuración botón stock */
    btn_stock.setOnClickListener() {
        activity.startActivity(Intent(activity, StockPersonal::class.java))
    }

    /* configuración botón foro */
    btn_foro.setOnClickListener() {
        activity.startActivity(Intent(activity, Foro::class.java))
    }

    /* configuración botón pedido */
    btn_pedido.setOnClickListener() {
        activity.startActivity(Intent(activity, PedidoHilos::class.java))
    }
    /* al hacerlo con activity, como función reutilizable, en vez de una clase
     es más eficiente. Hay que llamarlo en cada actividad, como he hecho en stock personal... */
}