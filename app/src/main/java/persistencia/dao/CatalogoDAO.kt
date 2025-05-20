package persistencia.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import persistencia.entidades.Catalogo

@Dao
interface CatalogoDAO {

    @Query("SELECT * FROM catalogo")
    suspend fun obtenerTodos(): List<Catalogo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(hilo: Catalogo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(hilos: List<Catalogo>)

    @Update
    suspend fun actualizar(hilo: Catalogo)

    @Delete
    suspend fun eliminar(hilo: Catalogo)

    @Query("DELETE FROM catalogo")
    suspend fun eliminarTodos()

    @Query("SELECT * FROM catalogo WHERE codigoHilo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Catalogo?
}
