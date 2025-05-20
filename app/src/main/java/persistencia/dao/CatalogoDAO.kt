import androidx.room.*

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
}
