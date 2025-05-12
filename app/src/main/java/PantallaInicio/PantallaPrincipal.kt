package PantallaInicio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R


class PantallaPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_aa_inicio)

        /* llamada a la función para usar el toolbar */
        Toolbar.funcionToolbar(this) // TODO de @olga he añadido esto para poder probar el toolbar, ruego me perdone


        //navegación a pantalla de datos personales a través de botón 'configuración'
        val configuracion = findViewById<ImageButton>(R.id.imgBtn_configuracion)

        //al tratarse de un 'imageButton' configuramos metodo 'onClick'
        configuracion.setOnClickListener() {
            val intent = Intent(this, DatosPersonales::class.java)
            startActivity(intent)
        }
    }
}