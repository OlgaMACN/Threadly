package logica.almacen_pedidos

object PedidoSingleton {

    val listaPedidos = mutableListOf<PedidoGuardado>()

    fun guardarPedido(pedido: PedidoGuardado) {
        val indexExistente = listaPedidos.indexOfFirst { it.nombre == pedido.nombre }
        if (indexExistente != -1) {
            listaPedidos[indexExistente] = pedido
        } else {
            listaPedidos.add(pedido)
        }
    }
}
