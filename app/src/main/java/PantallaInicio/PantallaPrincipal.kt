package PantallaInicio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import utiles.funcionToolbar


class PantallaPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        // TODO estas tres lineas son solo para pruebas de sesiones en memoria, borrar cuando haya persistencia
        val nombreUsuario = intent.getStringExtra("nombre_usuario")
        val textoBienvenida = findViewById<TextView>(R.id.txtVw_nombreUsuario)
        textoBienvenida.text = "Bienvenido/a, $nombreUsuario"


        //navegación a pantalla de datos personales a través de botón 'configuración'
        val configuracion = findViewById<ImageButton>(R.id.imgBtn_configuracion)

        //al tratarse de un 'imageButton' configuramos metodo 'onClick'
        configuracion.setOnClickListener() {
            val intent = Intent(this, DatosPersonales::class.java)
            startActivity(intent)
        }
    }
}