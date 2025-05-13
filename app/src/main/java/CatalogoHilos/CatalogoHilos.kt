package CatalogoHilos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import toolbar.funcionToolbar

class CatalogoHilos : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultar_aa_hilos)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        //declaramos elementos de esta pantalla
        //lupa no tendría que ser un imageButton???????
        val iconoLupa = findViewById<ImageView>(R.id.imgVw_lupaConsultar)

        val datoNumHilo = findViewById<EditText>(R.id.txtVw_buscarHiloConsulta)

        val btn_AgregarHilo = findViewById<Button>(R.id.btn_agregarHiloConsulta)

        val btn_ModificarHilo = findViewById<Button>(R.id.btn_modificarHiloConsulta)

        //configuracion boton agregar hilo
        btn_AgregarHilo.setOnClickListener() {

            val intentAgregarHilo = Intent(this, AgregarHilo::class.java)
            startActivity(intentAgregarHilo)
        }

        //configuracion boton modificar hilo
        btn_ModificarHilo.setOnClickListener() {

            val intentModificarHilo = Intent (this, ModificarHilo::class.java)
            startActivity(intentModificarHilo)
        }


    }

    //en esta pantalla se implementará la búsqueda de un hilo en función de su número
}