import android.content.Context
import androidx.room.Room
import persistencia.db.ThreadlyBdD

/* patrón singleton porque sólo habrá una instancia de la base de datos */
object GestorBBDD {

    @Volatile
    /* junto con synchronized, garantiza una única instancia de la BdD en toda la aplicación */
    private var INSTANCE: ThreadlyBdD? = null /* aquí se guardará dicha instancia */

    /* esta función comprueba si ya existe una instancia */
    fun getDatabase(context: Context): ThreadlyBdD {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ThreadlyBdD::class.java,
                "mihilos-db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
