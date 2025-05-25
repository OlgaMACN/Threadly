package utiles

/**
 * Objeto singleton que contiene una lista fija de consejos útiles para el bordado.
 *
 * No es una entidad persistente, por lo que no se guarda ni modifica en la base de datos.
 * Está diseñado para estar disponible globalmente y accesible desde cualquier parte de la aplicación.
 */
object Consejos {

    /**
     * Lista inmutable de consejos relacionados con la práctica del bordado.
     */
    val lista = listOf(
        "Cambia la aguja después de cada proyecto grande",
        "Usa la aguja correcta para cada tipo de tela",
        "Lávate tus manos antes de bordar, no querrás ensuciar la tela",
        "Evita trabajar con hilos demasiado largos",
        "Descansa cada cierto tiempo para no forzar la vista",
        "Marca con jaboncillo o lápiz de sastre, no con bolígrafo",
        "Hilvana cuando necesites más precisión",
        "Prelava las telas antes de cortar",
        "Si la tela resbala, rocía un poco de almidón antes de cortar",
        "El bastidor es tu mejor aliado a la hora de coser",
        "No dejes el bastidor puesto al guardar el trabajo",
        "No hagas nudos al empezar: ancla con puntadas ocultas",
        "Para punto de cruz, mantén siempre el mismo sentido en las X",
        "Plancha por el revés y con una toalla encima",
        "Lava el bordado a mano y con agua fría, sin frotar",
        "Usa agujas de punta roma para punto de cruz",
        "Usa agujas finas y afiladas para bordado libre",
        "Protege tu bordado con papel manteca si lo guardas a medias",
        "Da vueltas suaves al hilo cuando se enrede, no tires",
        "Marca el centro de la tela antes de empezar",
        "Usa tela Aida, lino o Lugana según tu nivel",
        "Disfruta cada cruz: es un mimo hecho hilo",
        "Usa una guía imantada o regla para no perderte",
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
