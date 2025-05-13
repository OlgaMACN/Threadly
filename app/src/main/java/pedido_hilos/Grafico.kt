package pedido_hilos

import grafico_pedido.HiloGrafico

/* clase que contiene los atributos de un gráfico de punto de cruz */
data class Grafico(
    val nombre: String,
    val countTela: Int,
    val madejas: Int = 0, /* como las madejas surgen de un cálculo, se inicializa en cero */
    val listaHilos: List<HiloGrafico> = emptyList() /* por defecto se crea vacía, para evitar errores, y se inicializa */
)
