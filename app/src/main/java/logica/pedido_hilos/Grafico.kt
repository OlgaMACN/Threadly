package logica.pedido_hilos

import logica.grafico_pedido.HiloGrafico
import java.io.Serializable

/**
 * Data class que representa un gráfico de punto de cruz dentro de un pedido.
 *
 * Un [Grafico] incluye:
 *  - Un nombre identificativo único dentro del pedido.
 *  - El tipo de tela (`countTela`) que determina la cantidad de hilos por pulgada.
 *  - La cantidad total de madejas necesarias (`madejas`), que se inicializa en 0
 *    y se calcula en función de los hilos añadidos.
 *  - La lista de hilos requeridos (`listaHilos`) para completar el gráfico,
 *    representada por instancias de [HiloGrafico].
 *
 * Esta clase implementa [Serializable] para poder enviarse entre actividades
 * mediante `Intent.putExtra("grafico", grafico)`.
 *
 * @property nombre Nombre único o identificativo del gráfico en el pedido.
 * @property countTela Número de hilos por pulgada (“count”) de la tela usada.
 * @property madejas Total de madejas necesarias para completar el gráfico.
 *                  Se calcula posteriormente tras editar o añadir hilos.
 * @property listaHilos Lista mutable de objetos [HiloGrafico] que representan
 *                      los hilos requeridos para este gráfico.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
data class Grafico(
    val nombre: String,
    val countTela: Int = 0,
    var madejas: Int = 0,
    var listaHilos: MutableList<HiloGrafico>
) : Serializable
