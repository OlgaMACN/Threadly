package CatalogoHilos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class CatalogoHilos : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogo_aa_hilos)

        //declaramos elementos de esta pantalla
        //lupa no tendría que ser un imageButton???????
        val iconoLupa = findViewById<ImageView>(R.id.imgVw_lupaConsultar)

        val datoNumHilo = findViewById<EditText>(R.id.txtVw_buscarHiloConsulta)

        val btn_AgregarHilo = findViewById<Button>(R.id.btn_agregarHiloConsulta)

        val btn_ModificarHilo = findViewById<Button>(R.id.btn_modificarHiloConsulta)

        //configuracion boton agregar hilo
        btn_AgregarHilo.setOnClickListener() {

        }

        //configuracion boton modificar hilo
        btn_ModificarHilo.setOnClickListener() {

        }


    }

    //en esta pantalla se implementará la búsqueda de un hilo en función de su número
}