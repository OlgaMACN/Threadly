package logica.grafico_pedido

import java.io.Serializable

/**
 * Data class que representa un hilo específico dentro de un gráfico de un pedido.
 *
 * Cada [HiloGrafico] incluye:
 *  - [hilo]: Identificador único del hilo (por ejemplo, su código en el catálogo).
 *  - [madejas]: Cantidad de madejas originalmente calculadas para este hilo en el gráfico.
 *  - [cantidadModificar]: Cantidad que el usuario ha introducido para modificar las madejas.
 *      Si es null, se considerará que no hay cambio y se usará [madejas].
 *
 * Se utiliza en la pantalla de edición de gráficos ([GraficoPedido]) para:
 *  - Mostrar el hilo y las madejas necesarias.
 *  - Permitir editar la cantidad mediante un [EditText], manteniendo la cantidad original si no se modifica.
 *  - Enviar objetos entre actividades mediante `Intent.putExtra("hiloGrafico", hiloGrafico)`.
 *
 * Implementa [Serializable] para facilitar el paso de instancias entre actividades.
 *
 * @property hilo Identificador único del hilo.
 * @property madejas Cantidad de madejas calculadas originalmente.
 * @property cantidadModificar Cantidad de madejas modificada por el usuario, o null si no cambia.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
data class HiloGrafico(
    val hilo: String,
    val madejas: Int,
    var cantidadModificar: Int? = null
) : Serializable
