package logica.pedido_hilos

import logica.grafico_pedido.HiloGrafico
import java.io.Serializable

/**
 * Clase de datos que representa un gráfico de punto de cruz dentro de un pedido.
 *
 * Cada gráfico contiene su nombre identificativo, el tipo de tela (count),
 * la cantidad total de madejas necesarias, y la lista de hilos que requiere.
 *
 * Esta clase implementa [Serializable] para poder ser enviada entre actividades.
 *
 * @property nombre Nombre único o identificativo del gráfico.
 * @property countTela Número de hilos por pulgada (count) de la tela usada en el gráfico.
 * @property madejas Total de madejas necesarias para completar el gráfico (calculado posteriormente).
 * @property listaHilos Lista mutable de objetos [HiloGrafico] que representan los hilos requeridos para el gráfico.
 */
data class Grafico(
    val nombre: String,
    val countTela: Int = 0,
    var madejas: Int = 0, /* se inicializa en 0; se calcula en función de los hilos */
    var listaHilos: MutableList<HiloGrafico> /* lista inicializable desde el constructor */
) : Serializable
