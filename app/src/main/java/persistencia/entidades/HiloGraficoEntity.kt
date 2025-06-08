package persistencia.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un hilo específico dentro de un gráfico de un pedido.
 *
 * Cada instancia corresponde a un hilo añadido a un gráfico, incluyendo la cantidad
 * de madejas que se requieren para ese hilo. Además, puede tener una cantidad opcional
 * para modificar (usada para operaciones temporales o ediciones).
 *
 * @property id ID autogenerado de la entidad.
 * @property graficoId ID del gráfico al que pertenece este hilo.
 * @property hilo Nombre o identificador del hilo.
 * @property madejas Cantidad actual de madejas requeridas de este hilo en el gráfico.
 * @property cantidadModificar Cantidad opcional para modificar, puede ser nula.
 *
 *@author Olga y Sandra Macías Aragón
 *
 */
@Entity(tableName = "hilos_grafico")
data class HiloGraficoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val graficoId: Int,
    val hilo: String,
    val madejas: Int,
    val cantidadModificar: Int? = null
)
