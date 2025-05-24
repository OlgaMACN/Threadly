package logica.grafico_pedido

import logica.pedido_hilos.Grafico

object GraficoSingleton {

    var grafico: Grafico? = null
        private set

    fun setGrafico(nuevoGrafico: Grafico) {
        grafico = nuevoGrafico
    }

    fun getListaHilos(): List<HiloGrafico> {
        return grafico?.listaHilos ?: emptyList()
    }

    fun agregarHilo(hilo: String, madejas: Int) {
        grafico?.listaHilos?.add(HiloGrafico(hilo, madejas))
        ordenarHilos()
    }

    fun eliminarHilo(hiloGrafico: HiloGrafico) {
        grafico?.listaHilos?.remove(hiloGrafico)
        ordenarHilos()
    }

    fun actualizarTotalMadejas() {
        grafico?.madejas = grafico?.listaHilos?.sumOf { it.cantidadModificar ?: it.madejas } ?: 0
    }

    fun ordenarHilos() {
        grafico?.let {
            it.listaHilos =
                utiles.funciones.ordenarHilos(it.listaHilos) { hilo -> hilo.hilo }.toMutableList()
        }
    }


}
