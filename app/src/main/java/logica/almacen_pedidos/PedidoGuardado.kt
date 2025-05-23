package logica.almacen_pedidos

import logica.pedido_hilos.Grafico
import java.io.Serializable

data class PedidoGuardado(
    val nombre: String,
    val graficos: List<Grafico>
) : Serializable

data class GraficoGuardado(
    val nombre: String,
    val listaHilos: MutableList<Hilo>
)

data class Hilo(
    val codigo: String,
    val madejas: Int
)
