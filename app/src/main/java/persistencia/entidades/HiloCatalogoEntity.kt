package persistencia.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un hilo dentro del catálogo de hilos de un usuario.
 *
 * Cada usuario tiene su propio catálogo personalizado de hilos, donde cada hilo
 * está identificado por un número único (`numHilo`) dentro de ese catálogo.
 *
 * Esta entidad almacena información básica del hilo, incluyendo un nombre descriptivo
 * y opcionalmente un color asociado.
 *
 * La combinación de `userId` y `numHilo` está indexada como única para evitar duplicados
 * del mismo número de hilo en un catálogo de usuario.
 *
 * @property id ID autogenerado del hilo en el catálogo.
 * @property userId ID del usuario propietario del catálogo donde está este hilo.
 * @property numHilo Número o código identificativo único del hilo en el catálogo del usuario.
 * @property nombreHilo Nombre descriptivo del hilo.
 * @property color Código de color hexadecimal (o similar) asociado al hilo, puede ser nulo.
 *
 * @author Olga y Sandra Macías Aragón
 */
@Entity(
    tableName = "hilo_catalogo",
    /* no se pueden insertar dos hilos con el mismo numHilo para el mismo userId */
    indices = [Index(value = ["userId", "numHilo"], unique = true)]
)
data class HiloCatalogoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val numHilo: String,
    val nombreHilo: String,
    val color: String? = null
)
