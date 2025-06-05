package utiles

/**
 * Objeto singleton que contiene una lista fija de consejos útiles para el bordado.
 *
 * No es una entidad persistente, por lo que no se guarda ni modifica en la base de datos.
 * Está diseñado para estar disponible globalmente y accesible desde cualquier parte de la aplicación.
 *
 * * @author Olga y Sandra Macías Aragón
 */
object Consejos {

    /**
     * Lista inmutable de consejos relacionados con la práctica del bordado.
     */
    val lista = listOf(
        "Conviene cambiar la aguja después de un proyecto grande",
        "Usar la aguja correcta para cada tipo de tela es clave",
        "Es recomendable lavarse tus manos antes de bordar, no querrás ensuciar la tela",
        "Evita trabajar con hilos demasiado largos, se enredan y es un fastidio",
        "Descansa cada cierto tiempo para no forzar la vista",
        "Mejor marcar la tela con jaboncillo o lápiz de sastre, en vez de usar un bolígrafo",
        "Te recomendamos hilvanar cuando necesites más precisión para tu labor",
        "Prelavar las telas antes de cortar evita el encogimiento y la decoloración",
        "Si la tela resbala, rocía un poquito de almidón antes de cortar",
        "El bastidor es tu mejor aliado a la hora de coser",
        "No conviene dejar el bastidor puesto al guardar el trabajo",
        "No hagas nudos al empezar: ancla con puntadas ocultas y notarás la diferencia",
        "Para punto de cruz, mantén siempre el mismo sentido en las X",
        "Plancha por el revés y con una toalla encima",
        "Lavar el bordado a mano y con agua fría sin frotar, evitará dañar la tela",
        "Usa agujas de punta roma para punto de cruz",
        "Usa agujas finas y afiladas para bordado libre",
        "Protege tu bordado con papel manteca si lo guardas a medias",
        "Da vueltas suaves al hilo cuando se enrede, no tires",
        "Marca el centro de la tela antes de empezar",
        "Usa tela Aida, lino o Lugana según tu nivel",
        "Disfruta cada cruz: es un mimo hecho hilo",
        "¿Has probado a usar una guía imantada o regla para no perderte?",
        "Cose en un espacio bien iluminado, no queremos que tus ojitos sufran"
        )

    /**
     * Devuelve un consejo aleatorio de la lista.
     *
     * @return Un String con un consejo seleccionado al azar.
     */
    fun obtenerAleatorio(): String {
        return lista.random()
    }
}
