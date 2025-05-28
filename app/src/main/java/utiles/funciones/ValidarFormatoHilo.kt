package utiles.funciones

/**
 * Objeto de utilidad para validar el formato de los códigos de hilos.
 *
 * Esta clase proporciona una función que comprueba si un código de hilo
 * contiene únicamente caracteres alfanuméricos (letras y/o números).
 *
 * No se permiten espacios, símbolos ni caracteres especiales.
 */
object ValidarFormatoHilos {

    /* expresión regular que acepta sólo letras y números, sin espacios ni símbolos */
    private val regexValido = Regex("^[A-Za-z0-9]+$")

    /**
     * Verifica si el código de hilo tiene un formato válido.
     *
     * Un código es considerado válido si contiene únicamente caracteres alfabéticos
     * (mayúsculas o minúsculas) y/o numéricos, sin espacios, guiones u otros símbolos.
     *
     * @param hilo El código del hilo a validar.
     * @return `true` si el formato es válido, `false` en caso contrario.
     *
     * ### Ejemplos válidos:
     * - "310"
     * - "B5200"
     * - "BLANC"
     *
     * ### Ejemplos inválidos:
     * - "123*"
     * - "abc 123"
     * - "C-310"ç
     * * @author Olga y Sandra Macías Aragón
     */
    fun formatoValidoHilo(hilo: String): Boolean {
        return hilo.matches(regexValido)
    }
}
