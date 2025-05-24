package logica.pantalla_inicio

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.threadly.R
import kotlinx.coroutines.launch
import logica.stock_personal.StockSingleton
import utiles.BaseActivity
import utiles.funciones.funcionToolbar

class PantallaPrincipal : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)

        funcionToolbar(this) /* llamada a la función para usar el toolbar */
        cargarUsuario()

        /* obtener número de madejas del stock */
        StockSingleton.inicializarStockSiNecesario(this)
        val totalMadejas = StockSingleton.mostrarTotalStock()

        val txtStock = findViewById<TextView>(R.id.txtVw_contenidoStock)
        txtStock.text = "$totalMadejas"

        /* mostrar nombre del usuario */
        findViewById<TextView>(R.id.txtVw_nombreUsuario).text = nombreUsuario

        /* mostrar tip aleatorio (sin Room) */
        val txtTip = findViewById<TextView>(R.id.txtVw_contenidoTip)
        txtTip.text = obtenerConsejoAleatorio()

        /* ir a datos personales */
        findViewById<ImageButton>(R.id.imgBtn_configuracion).setOnClickListener {
            irAActividad(DatosPersonales::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarUsuario()

        StockSingleton.inicializarStockSiNecesario(this)
        val total = StockSingleton.mostrarTotalStock()
        findViewById<TextView>(R.id.txtVw_contenidoStock).text = "$total"
    }

    private fun cargarUsuario() {
        val usuario = DatosPersonales.usuarioEnMemoria
        usuario?.let {
            findViewById<ImageView>(R.id.imgVw_imagenPerfil).setImageResource(
                when (it.idImagen) {
                    1 -> R.drawable.img_avatar2
                    2 -> R.drawable.img_avatar3
                    3 -> R.drawable.img_avatar4
                    4 -> R.drawable.img_avatar5
                    5 -> R.drawable.img_avatar6
                    6 -> R.drawable.img_avatar_defecto
                    else -> R.drawable.img_avatar_defecto
                }
            )
            findViewById<TextView>(R.id.txtVw_nombreUsuario).text = it.nombre
        }
    }

    private fun obtenerConsejoAleatorio(): String {
        val consejos = listOf(
            "Organiza tus hilos por colores.",
            "Etiqueta tus madejas para no perder el número.",
            "Guarda tus gráficos en carpetas por dificultad.",
            "Usa una luz blanca al bordar de noche.",
            "Haz pausas para evitar cansancio visual.",
            "Consejo no disponible"
        )
        return consejos.random()
    }
}
