package logica.stock_personal

/* TODO comentar a Sandra: quería meterla en un paquete 'patrones' pero sino no me pilla la lista del stock */
/* esta clase sirve para que haya una única instancia de la lista, accesible desde cualquier otra clase */
object StockSingleton {
    val listaStock: MutableList<HiloStock> = mutableListOf()

    fun obtenerMadejas(hiloId: String): Int? {
        return listaStock.find { it.hiloId == hiloId }?.madejas
    }

    fun agregarHilo(hilo: String, madejas: Int) {
        listaStock.add(HiloStock(hilo, madejas))
    }
}