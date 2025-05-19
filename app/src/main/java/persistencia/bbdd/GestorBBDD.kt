package persistencia.bbdd

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import persistencia.entidades.Consejo

/* patrón singleton porque sólo habrá una instancia de la base de datos */
object GestorBBDD {

    @Volatile
    /* junto con synchronized, garantiza una única instancia de la BdD en toda la aplicación */
    private var INSTANCE: ThreadlyBdD? = null /* aquí se guardará dicha instancia */

    /* esta función comprueba si ya existe una instancia */
    fun getDatabase(context: Context): ThreadlyBdD {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder( /* aquí se construye la base de datos */
                context.applicationContext,
                ThreadlyBdD::class.java,
                "mihilos-db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val consejos = listOf(
                                Consejo(contenido = "Escoge un proyecto acorde a tu nivel, ¡no te frustres!"),
                                Consejo(contenido = "El algodón es ideal para principiantes"),
                                Consejo(contenido = "Es mejor lavar la tela antes de cortar"),
                                Consejo(contenido = "¡Un pedido bien hecho ahorra tiempo después!"),
                                Consejo(contenido = "¿Sabías que hay un montón de patrones en PDF?"),
                                Consejo(contenido = "Conocer los tipos de agujas que existen es esencial para decidirse"),
                                Consejo(contenido = "Marcar los patrones antes de empezar es una buena práctica"),
                                Consejo(contenido = "Lavarse las manos antes de coser evitará manchas indeseadas en tu labor"),
                                Consejo(contenido = "Es mejor empezar a medir la tela desde el centro"),
                                Consejo(contenido = "No descuides tu postura a la hora de coser"),
                                Consejo(contenido = "Coser en un espacio bien iluminado será mejor para tus ojitos"),
                                Consejo(contenido = "Resulta más conveniente coser con dos hebras que con una"),
                                Consejo(contenido = "Los bastidores son los mejores aliados de la costura"),
                                Consejo(contenido = "No conviene que tenses demasiado los puntos al coser, puede deformar tus puntadas"),
                                Consejo(contenido = "Recontar los puntos de vez en cuando te ahorrará muchos dolores de cabeza"),
                                Consejo(contenido = "Deja colgar la aguja con el hilo de vez en cuando y verás cómo empieza a dar vueltas... ¡Te evitará muchos nudos!"),
                                Consejo(contenido = "Plancha el diseño terminado del revés"),
                            )
                            getDatabase(context).consejoDao()
                                .insertarTodos(*consejos.toTypedArray())
                        }
                    }
                })
                .build()

            INSTANCE = instance
            instance
        }
    }
}
