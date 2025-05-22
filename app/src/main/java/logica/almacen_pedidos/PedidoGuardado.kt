package logica.almacen_pedidos

import logica.pedido_hilos.Grafico

data class PedidoGuardado(
    val nombre: String,
    val graficos: List<Grafico>
)

