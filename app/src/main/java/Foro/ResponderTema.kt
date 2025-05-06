package Foro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class ResponderTema : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comunidad_respuesta_tema)

        //declaramos elementos de esta pantalla
        val btn_enviar = findViewById<Button>(R.id.btn_enviarRespuestaForo)

        val btn_volver = findViewById<Button>(R.id.btn_volverCrearTema)

        val respuestaUsuario = findViewById<EditText>(R.id.edtTxt_respuestaForo)

        //configuracion boton volver
        btn_volver.setOnClickListener() {

            val intentVolverTema = Intent (this, VisualizarTema::class.java)
            startActivity(intentVolverTema)
        }
    }
}