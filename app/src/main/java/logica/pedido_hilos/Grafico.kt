package logica.pedido_hilos

import logica.grafico_pedido.HiloGrafico
import java.io.Serializable

/* clase que contiene los atributos de un gráfico de punto de cruz */
data class Grafico(
    val nombre: String,
    val countTela: Int = 0,
    var madejas: Int = 0, /* como las madejas surgen de un cálculo, se inicializa en cero */
    var listaHilos: MutableList<HiloGrafico> /* por defecto se crea vacía, para evitar errores, y se inicializa */
) : Serializable
