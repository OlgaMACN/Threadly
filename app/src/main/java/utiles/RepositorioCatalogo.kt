package utiles

import Catalogo
import CatalogoDAO
import android.content.Context
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utiles.funciones.leerXML

class RepositorioCatalogo(
    private val dao: CatalogoDAO,
    private val context: Context
) {
    suspend fun inicializarCatalogoSiVacio() = withContext(Dispatchers.IO) {
        val listaActual = dao.obtenerTodos()
        if (listaActual.isEmpty()) {
            val hilos = leerXML(context, R.raw.catalogo_hilos)
            dao.insertarTodos(hilos)
        }
    }

    suspend fun obtenerCatalogo(): List<Catalogo> = withContext(Dispatchers.IO) {
        dao.obtenerTodos()
    }

    suspend fun actualizarHilo(hilo: Catalogo) = withContext(Dispatchers.IO) {
        dao.actualizar(hilo)
    }

    suspend fun eliminarHilo(hilo: Catalogo) = withContext(Dispatchers.IO) {
        dao.eliminar(hilo)
    }

    suspend fun insertarHilo(hilo: Catalogo) = withContext(Dispatchers.IO) {
        dao.insertar(hilo)
    }
}