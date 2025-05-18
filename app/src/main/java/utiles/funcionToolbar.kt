package utiles

import logica.CatalogoHilos.CatalogoHilos
import logica.PantallaInicio.PantallaPrincipal
import android.app.Activity
import android.content.Intent
import android.widget.ImageButton
import com.threadly.R
import logica.pedido_hilos.PedidoHilos
import logica.stock_personal.StockPersonal

private var ultimoClick = 0L

fun funcionToolbar(activity: Activity) {
    /* inicialización de botones del toolbar */
    val btn_inicio = activity.findViewById<ImageButton>(R.id.botonInicio)
    val btn_catalogo = activity.findViewById<ImageButton>(R.id.botonCatalogo)
    val btn_stock = activity.findViewById<ImageButton>(R.id.botonStock)
    val btn_pedido = activity.findViewById<ImageButton>(R.id.botonPedido)
    val btn_almacen_pedidos = activity.findViewById<ImageButton>(R.id.botonAlmacenPedido)

    /* si el usuario acaba de hacer clic se bloquea para no crashear */
    fun clicSeguro(interval: Long = 1000L, block: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - ultimoClick > interval) {
            ultimoClick = currentTime
            block()
        }
    }

    /* sólo funciona si no es la activity actual */
    fun siNoEsActivityActual(target: Class<out Activity>) {
        if (activity::class.java != target) {
            val intent = Intent(activity, target)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    /* configuración botón catálogo */
    btn_catalogo.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(CatalogoHilos::class.java)
        }
    }

    /* configuración botón stock */
    btn_stock.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(StockPersonal::class.java)
        }
    }


    /* configuración botón inicio */
    btn_inicio.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(PantallaPrincipal::class.java)
        }
    }

    /* configuración botón pedido */
    btn_pedido.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(PedidoHilos::class.java)
        }
    }

    /* configuración botón almacén pedidos */
    btn_pedido.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(PedidoHilos::class.java)
        }
    }

}