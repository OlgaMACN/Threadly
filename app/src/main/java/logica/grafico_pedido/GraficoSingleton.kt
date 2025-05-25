package logica.grafico_pedido

import logica.pedido_hilos.Grafico

/**
 * Objeto singleton que mantiene en memoria el gráfico actualmente seleccionado
 * para su edición dentro de un pedido.
 *
 * Su propósito es facilitar el acceso global al gráfico activo sin necesidad
 * de pasarlo continuamente entre actividades o fragmentos.
 *
 * Este singleton se reinicia solo si se sobrescribe con un nuevo gráfico.
 */
object GraficoSingleton {

    /**
     * Gráfico actualmente cargado en memoria.
     * Es privado para evitar modificaciones externas directas.
     */
    var grafico: Grafico? = null
        private set

    /**
     * Asigna un nuevo gráfico al singleton.
     *
     * @param nuevoGrafico Gráfico a cargar para edición o visualización.
     */
    fun setGrafico(nuevoGrafico: Grafico) {
        grafico = nuevoGrafico
    }

    /**
     * Obtiene la lista de hilos del gráfico actualmente cargado.
     *
     * @return Lista de [HiloGrafico] o lista vacía si no hay gráfico cargado.
     */
    fun getListaHilos(): List<HiloGrafico> {
        return grafico?.listaHilos ?: emptyList()
    }

    /**
     * Agrega un nuevo hilo al gráfico actual.
     *
     * @param hilo Código o nombre del hilo a agregar.
     * @param madejas Cantidad de madejas necesarias para este hilo.
     */
    fun agregarHilo(hilo: String, madejas: Int) {
        grafico?.listaHilos?.add(HiloGrafico(hilo, madejas))
        ordenarHilos()
    }

    /**
     * Elimina un hilo específico del gráfico actual.
     *
     * @param hiloGrafico El hilo a eliminar de la lista.
     */
    fun eliminarHilo(hiloGrafico: HiloGrafico) {
        grafico?.listaHilos?.remove(hiloGrafico)
        ordenarHilos()
    }

    /**
     * Recalcula y actualiza el total de madejas del gráfico sumando
     * los valores modificados o, en su defecto, los valores originales.
     */
    fun actualizarTotalMadejas() {
        grafico?.madejas = grafico?.listaHilos?.sumOf { it.cantidadModificar ?: it.madejas } ?: 0
    }

    /**
     * Ordena alfabéticamente la lista de hilos del gráfico por su identificador.
     *
     * Usa la función `ordenarHilos` definida en `utiles.funciones`.
     */
    fun ordenarHilos() {
        grafico?.let {
            it.listaHilos = utiles.funciones
                .ordenarHilos(it.listaHilos) { hilo -> hilo.hilo }
                .toMutableList()
        }
    }
}
