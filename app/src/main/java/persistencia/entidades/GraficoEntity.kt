package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un gráfico dentro de un pedido de hilos.
 *
 * Un gráfico es un grupo de hilos asociado a un diseño específico dentro de un pedido.
 * Puede contener varios hilos y tener un nombre único por usuario dentro de un mismo pedido.
 *
 * Esta entidad forma parte de la relación uno-a-muchos con `HiloGraficoEntity` (a través de `graficoId`)
 * y también se relaciona con `PedidoEntity` mediante `idPedido`.
 *
 * La combinación de `userId`, `nombre` e `idPedido` está indexada como única para evitar duplicados
 * del mismo gráfico dentro de un pedido del mismo usuario.
 *
 * @property id ID autogenerado del gráfico.
 * @property nombre Nombre identificativo del gráfico.
 * @property idPedido ID del pedido al que pertenece (puede ser `null` si está en preparación).
 * @property userId ID del usuario propietario del gráfico.
 * @property count Cantidad de tela (en cm) asociada al gráfico. Puede ser nulo si no se ha definido aún.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
@Entity(
    tableName = "graficos",
    indices = [Index(value = ["userId", "nombre", "idPedido"], unique = true)]
)
data class GraficoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val idPedido: Int?, /* null = pedido en curso */
    val userId: Int,
    val count: Int? = null
)
