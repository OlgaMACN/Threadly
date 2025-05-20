package utiles.funciones

import logica.catalogo_hilos.HiloCatalogo
import persistencia.entidades.Catalogo

/* convertir 'Catalogo' (entidad) a 'HiloCatalogo' (lógica)*/
fun Catalogo.toHiloCatalogo(): HiloCatalogo {
    return HiloCatalogo(
        numHilo = this.codigoHilo,
        nombreHilo = this.nombreHilo,
        color = this.color
    )
}

/* convertir 'HiloCatalogo' a 'Catalogo' (entidad) */
fun HiloCatalogo.toCatalogo(): Catalogo {
    return Catalogo(
        codigoHilo = this.numHilo,
        nombreHilo = this.nombreHilo,
        color = this.color
    )
}