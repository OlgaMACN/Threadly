package persistencia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import persistencia.entidades.Consejo

@Dao
interface ConsejoDAO {
    @Query("SELECT * FROM consejos ORDER BY RANDOM() LIMIT 1")
    suspend fun obtenerAleatorio(): Consejo?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(vararg consejos: Consejo)
}