package Foro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class Foro : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comunidad_aa_foro)

        /* llamada a la función para usar el toolbar */
        Toolbar.funcionToolbar(this) // TODO de @olga he añadido esto para poder probar el toolbar, ruego me perdone

        //TODO CONFIGURAR VISTA DE TEMAS Y PODER PULSAR UNO PARA VISUALIZARLO CON SUS RESPUESTAS

        //declaramos elementos de pantalla
        val btn_crearTema = findViewById<ImageButton>(R.id.imgBtn_CrearTema)


        //configuracion para crear tema
        btn_crearTema.setOnClickListener() {

            val intentCrearTema = Intent(this, CrearTema::class.java)
            startActivity(intentCrearTema)
        }
    }
}