package utiles

import android.content.Context
import com.threadly.R
import persistencia.bbdd.ThreadlyDatabase
import persistencia.entidades.HiloCatalogoEntity
import persistencia.entidades.HiloStockEntity
import utiles.funciones.LeerXMLCodigo
import utiles.funciones.leerXML

object PrecargaDatos {
    suspend fun precargarCatalogoYStockSiNoExisten(context: Context, userId: Int) {
        val db = ThreadlyDatabase.getDatabase(context)
        val hiloCatalogoDao = db.hiloCatalogoDao()
        val hiloStockDao    = db.hiloStockDao()

        // 1) Si el usuario no tiene ningún hilo en el catálogo, volcamos el XML
        val countCatalogo = hiloCatalogoDao.contarHilosPorUsuario(userId)
        if (countCatalogo == 0) {
            // lee lista de "HiloCatalogoXML" con leerXML(...)
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

        // 2) Si el usuario no tiene ningún hilo en stock, volcamos stock a 0
        val countStock = hiloStockDao.contarStocksPorUsuario(userId)
        if (countStock == 0) {
            // Leer XML con códigos (podría usarse la misma lista)
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