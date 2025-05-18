package logica.pantalla_inicio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logica.stock_personal.StockSingleton
import persistencia.bbdd.GestorBBDD
import utiles.funcionToolbar


class PantallaPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* obtener número de madejas del stock */
        val totalMadejas = StockSingleton.listaStock.sumOf { it.madejas }

        /* mostrar el total en txtVw_contenidoStock */
        val txtStock = findViewById<TextView>(R.id.txtVw_contenidoStock)
        txtStock.text = "$totalMadejas"

        /* para mostrar el nombre del usuario al entrar (pasado desde login) */
        val nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Usuario"
        val txtNombreUsuario = findViewById<TextView>(R.id.txtVw_nombreUsuario)
        txtNombreUsuario.text = nombreUsuario

        /* mostrar tip aleatorio */
        val txtTip = findViewById<TextView>(R.id.txtVw_contenidoTip)
        val db = GestorBBDD.getDatabase(this)
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
            val intent = Intent(this, DatosPersonales::class.java)
            startActivity(intent)
        }
    }
}