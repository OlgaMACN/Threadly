package logica.almacen_pedidos

import logica.pedido_hilos.Grafico

data class PedidoGuardado(
    val nombre: String,
    val graficos: List<Grafico>
)

data class GraficoGuardado(
    val nombre: String,
    val listaHilos: MutableList<Hilo>
)

data class Hilo(
    val codigo: String,
    val madejas: Int
)
