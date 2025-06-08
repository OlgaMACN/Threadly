package utiles.funciones

import android.widget.ImageButton
import com.threadly.R
import logica.almacen_pedidos.AlmacenPedidos
import logica.catalogo_hilos.CatalogoHilos
import logica.pantalla_inicio.PantallaPrincipal
import logica.pedido_hilos.PedidoHilos
import logica.stock_personal.StockPersonal
import utiles.BaseActivity

private var ultimoClick = 0L

/**
 * Configura el comportamiento del toolbar común en las pantallas principales de la app.
 *
 * Esta función enlaza los botones del toolbar con sus respectivas pantallas:
 * Inicio, Catálogo, Stock, Pedido actual y Almacén de pedidos.
 *
 * También se encarga de evitar múltiples clics rápidos que puedan causar cierres inesperados
 * y previene la navegación redundante hacia la misma pantalla.
 *
 * @param activity La actividad actual desde donde se está configurando el toolbar.
 *                 Debe heredar de [BaseActivity] para poder compartir el contexto del usuario.
 *
 * ### Botones configurados:
 * - `botonInicio`: Navega a la pantalla principal.
 * - `botonCatalogo`: Navega al catálogo de hilos.
 * - `botonStock`: Navega al inventario personal.
 * - `botonPedido`: Navega al pedido actual.
 * - `botonAlmacenPedido`: Navega al almacén de pedidos guardados.
 *
 * @author Olga y Sandra Macías Aragón
 */
fun funcionToolbar(activity: BaseActivity) {
    /* inicialización de los botones del toolbar */
    val btn_inicio = activity.findViewById<ImageButton>(R.id.botonInicio)
    val btn_catalogo = activity.findViewById<ImageButton>(R.id.botonCatalogo)
    val btn_stock = activity.findViewById<ImageButton>(R.id.botonStock)
    val btn_pedido = activity.findViewById<ImageButton>(R.id.botonPedido)
    val btn_almacen_pedidos = activity.findViewById<ImageButton>(R.id.botonAlmacenPedido)

    /**
     * Ejecuta una acción solo si ha pasado un intervalo desde el último clic.
     * Previene errores por clics múltiples rápidos.
     */
    fun clicSeguro(interval: Long = 1000L, block: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - ultimoClick > interval) {
            ultimoClick = currentTime
            block()
        }
    }

    /**
     * Navega a otra actividad solo si no es la actual.
     * Si se llama desde [PedidoHilos], espera a que se completen posibles operaciones pendientes.
     */
    fun siNoEsActivityActual(target: Class<out BaseActivity>) {
        if (activity::class.java != target) {
            activity.irAActividad(target)
            activity.finish()
        }
    }

    /*
     * ┌─────────────────────────────────────────────┐
     * │ Configuración de cada botón del toolbar :D  │
     * └─────────────────────────────────────────────┘
     */

    btn_catalogo.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(CatalogoHilos::class.java)
        }
    }

    btn_stock.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(StockPersonal::class.java)
        }
    }

    btn_inicio.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(PantallaPrincipal::class.java)
        }
    }

    btn_pedido.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(PedidoHilos::class.java)
        }
    }

    btn_almacen_pedidos.setOnClickListener {
        clicSeguro {
            siNoEsActivityActual(AlmacenPedidos::class.java)
        }
    }
}
