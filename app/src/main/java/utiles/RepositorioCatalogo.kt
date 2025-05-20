package utiles

import android.content.Context
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import persistencia.bbdd.CatalogoBdD
import persistencia.entidades.Catalogo
import utiles.funciones.leerXML

/* uso de repositorio para interactuar con la BdD, es más limpio, eficiente y facilita las gestiones */
class RepositorioCatalogo(private val context: Context) {

    private val dao = CatalogoBdD.getDatabase(context).catalogoDao()

    suspend fun inicializarCatalogoSiEsNecesario() {
        withContext(Dispatchers.IO) {
            val existentes = dao.obtenerTodos()
            if (existentes.isEmpty()) {
                val desdeXml = leerXML(context, R.raw.catalogo_hilos)
                dao.insertarTodos(desdeXml)
            }
        }
    }

    suspend fun obtenerCatalogo(): List<Catalogo> {
        return withContext(Dispatchers.IO) {
            dao.obtenerTodos()
        }
    }

    suspend fun agregarHilo(hilo: Catalogo) {
        withContext(Dispatchers.IO) {
            dao.insertar(hilo)
        }
    }

    suspend fun actualizarHilo(hilo: Catalogo) {
        withContext(Dispatchers.IO) {
            dao.actualizar(hilo)
        }
    }

    suspend fun eliminarHilo(hilo: Catalogo) {
        withContext(Dispatchers.IO) {
            dao.eliminar(hilo)
        }
    }

    suspend fun buscarPorCodigo(codigo: String): Catalogo? {
        return withContext(Dispatchers.IO) {
            dao.buscarPorCodigo(codigo)
        }
    }
}
