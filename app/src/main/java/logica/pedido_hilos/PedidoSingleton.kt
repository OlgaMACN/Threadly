package logica.almacen_pedidos

/**
 * Singleton que mantiene en memoria una lista mutable de pedidos guardados.
 *
 * Esta clase actúa como un repositorio temporal en memoria para almacenar y gestionar
 * los pedidos realizados por el usuario durante la ejecución de la app.
 *
 * Funcionalidades principales:
 * - Guardar un pedido nuevo.
 * - Actualizar un pedido existente si ya hay uno con el mismo nombre.
 *
 * Nota: Esta implementación es volátil y los datos se perderán al cerrar la app,
 * por lo que se recomienda complementar con persistencia en base de datos.
 */
object PedidoSingleton {

    /**
     * Lista mutable que contiene todos los pedidos guardados en memoria.
     */
    val listaPedidos = mutableListOf<PedidoGuardado>()

    /**
     * Guarda un pedido en la lista. Si ya existe un pedido con el mismo nombre,
     * lo actualiza con la nueva información; en caso contrario, lo añade.
     *
     * @param pedido PedidoGuardado a guardar o actualizar.
     */
    fun guardarPedido(pedido: PedidoGuardado) {
        val indexExistente = listaPedidos.indexOfFirst { it.nombre == pedido.nombre }
        if (indexExistente != -1) {
            listaPedidos[indexExistente] = pedido
        } else {
            listaPedidos.add(pedido)
        }
    }
}
