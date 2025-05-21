package logica.pantalla_inicio

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.stock_personal.StockSingleton
import persistencia.bbdd.ThreadlySingleton
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

        /* mostrar el total en txtVw_contenidoStock */
        val txtStock = findViewById<TextView>(R.id.txtVw_contenidoStock)
        txtStock.text = "$totalMadejas"

        /* para mostrar el nombre del usuario al entrar (pasado desde login) */
        findViewById<TextView>(R.id.txtVw_nombreUsuario).text = nombreUsuario

        /* mostrar tip aleatorio */
        val txtTip = findViewById<TextView>(R.id.txtVw_contenidoTip)
        val db = ThreadlySingleton.getDatabase(this)
        val consejoDao = db.consejoDao()

        CoroutineScope(Dispatchers.IO).launch {
            val consejo = consejoDao.obtenerAleatorio()
            withContext(Dispatchers.Main) {
                txtTip.text = consejo?.contenido ?: "Consejo no disponible"
            }
        }

        /* navegación a pantalla de datos personales a través de botón 'configuración' */
        val configuracion = findViewById<ImageButton>(R.id.imgBtn_configuracion)

        /* al tratarse de un 'imageButton' configuramos metodo 'onClick' */
        configuracion.setOnClickListener() {
            irAActividad(DatosPersonales::class.java)
        }
    }

    /* al volver de DatosPersonales.kt se actualizarán los datos */
    override fun onResume() {
        super.onResume()
        cargarUsuario()

        /* actualizar madejas del stock*/
        StockSingleton.inicializarStockSiNecesario(this)
        val totalMadejas = StockSingleton.mostrarTotalStock()
        findViewById<TextView>(R.id.txtVw_contenidoStock).text = "$totalMadejas"
    }

    /* función para cargar el usuario*/
    private fun cargarUsuario() {
        val bbdd = ThreadlySingleton.getDatabase(this)
        val usuarioDao = bbdd.usuarioDao()

        CoroutineScope(Dispatchers.IO).launch {
            val usuario = usuarioDao.obtenerPorId(usuarioId)
            withContext(Dispatchers.Main) {
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
        }
    }

}