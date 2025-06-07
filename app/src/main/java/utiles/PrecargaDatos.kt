package utiles

import android.content.Context
import com.threadly.R
import persistencia.bbdd.ThreadlyDatabase
import persistencia.entidades.HiloCatalogoEntity
import persistencia.entidades.HiloStockEntity
import utiles.funciones.LeerXMLCodigo
import utiles.funciones.leerXML

/**
 * Objeto singleton responsable de precargar datos iniciales para un usuario.
 *
 * Esta clase verifica si un usuario tiene datos en las tablas de catálogo y stock de hilos,
 * y en caso negativo, carga los datos desde archivos XML predefinidos.
 *
 * Esto asegura que cada usuario tenga un catálogo y stock inicial para trabajar,
 * evitando datos vacíos o inconsistentes al crear nuevas cuentas.
 *
 * Método principal:
 * - [precargarCatalogoYStockSiNoExisten]: comprueba y carga los datos iniciales si es necesario.
 *
 * @author Olga y Sandra Macías Aragón
 *
 */
object PrecargaDatos {

    /**
     * Precarga el catálogo de hilos y el stock inicial (madejas = 0) para un usuario específico
     * si no existen ya datos en la base de datos.
     *
     * @param context Contexto para acceder a recursos y base de datos.
     * @param userId Identificador del usuario para el que se realiza la precarga.
     *
     * La función realiza:
     * 1) Consulta la cantidad de hilos en catálogo para el usuario.
     *    Si es cero, lee el XML `catalogo_hilos` y lo inserta en la tabla `HiloCatalogoEntity`.
     * 2) Consulta la cantidad de stocks de hilos para el usuario.
     *    Si es cero, lee el XML con códigos y crea entradas con `madejas = 0` en `HiloStockEntity`.
     *
     * Esta función es `suspend` y debe llamarse desde una coroutine o contexto asincrónico.
     */
    suspend fun precargarCatalogoYStockSiNoExisten(context: Context, userId: Int) {
        val db = ThreadlyDatabase.getDatabase(context)
        val hiloCatalogoDao = db.hiloCatalogoDao()
        val hiloStockDao    = db.hiloStockDao()

        /* si el usuario no tiene ningún hilo en el catálogo, se vuelca el XML */
        val countCatalogo = hiloCatalogoDao.contarHilosPorUsuario(userId)
        if (countCatalogo == 0) {
            /* lee lista de "HiloCatalogoXML" con leerXML */
            val xmlList = leerXML(context, R.raw.catalogo_hilos)
            val entidadesXml = xmlList.map { hc ->
                HiloCatalogoEntity(
                    userId    = userId,
                    numHilo   = hc.numHilo,
                    nombreHilo = hc.nombreHilo,
                    color     = hc.color
                )
            }
            hiloCatalogoDao.insertarHilos(entidadesXml)
        }

        /* si el usuario no tiene ningún hilo en stock, se vuelca el stock */
        val countStock = hiloStockDao.contarStocksPorUsuario(userId)
        if (countStock == 0) {
            /* leer XML, sololos  códigos */
            val xmlStock = LeerXMLCodigo(context, R.raw.catalogo_hilos)
            val entidadesStock = xmlStock.map { hilo ->
                HiloStockEntity(
                    usuarioId = userId,
                    hiloId    = hilo.hiloId,
                    madejas   = 0
                )
            }
            hiloStockDao.insertarStocks(entidadesStock)
        }
    }
}
