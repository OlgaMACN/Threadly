package utiles.funciones

object ValidarFormatoHilos {

    private val regexValido = Regex("^[A-Za-z0-9]+$")

    fun formatoValidoHilo(hilo: String): Boolean {
        return hilo.matches(regexValido)
    }
}
