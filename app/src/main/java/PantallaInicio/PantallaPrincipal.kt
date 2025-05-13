package PantallaInicio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import toolbar.funcionToolbar


class PantallaPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */



        //navegación a pantalla de datos personales a través de botón 'configuración'
        val configuracion = findViewById<ImageButton>(R.id.imgBtn_configuracion)

        //al tratarse de un 'imageButton' configuramos metodo 'onClick'
        configuracion.setOnClickListener() {
            val intent = Intent(this, DatosPersonales::class.java)
            startActivity(intent)
        }
    }
}