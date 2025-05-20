package utiles

import android.widget.ImageButton
import com.threadly.R
import logica.almacen_pedidos.AlmacenPedidos
import logica.catalogo_hilos.CatalogoHilos
import logica.pantalla_inicio.PantallaPrincipal
import logica.pedido_hilos.PedidoHilos
import logica.stock_personal.StockPersonal

private var ultimoClick = 0L

/* con esta clase habrá siempre acceso a usuarioId y nombre desde cualquier pantalla */
fun funcionToolbar(activity: BaseActivity) {
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

    /* el toolbar sólo funciona si no es la activity actual */
    fun siNoEsActivityActual(target: Class<out BaseActivity>) {
        if (activity::class.java != target) {
            activity.irAActividad(target)
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
    btn_almacen_pedidos.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(AlmacenPedidos::class.java)
        }
    }
}