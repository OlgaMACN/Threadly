package funciones

import org.junit.Assert.*

import org.junit.Test
import utiles.funciones.calcularMadejas

class CalcularMadejasTest {

    @Test
    fun calcularMadejasTest_OK() {

        val resultado = calcularMadejas(puntadas = 1200, countTela = 14)
        assertEquals(4,resultado)
    }

    @Test
    fun calcularMadejasTest_valorCero(){
        val resultado = calcularMadejas(puntadas = 0, countTela = 14)
        assertEquals(0,resultado)
    }
}