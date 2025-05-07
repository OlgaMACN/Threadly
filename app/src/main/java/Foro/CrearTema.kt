package Foro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class CrearTema : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comunidad_pantalla_crear_tema)

        //declaramos elementos de esta pantalla
        val contenidoTitulo = findViewById<EditText>(R.id.editTxt_hintTituloTema)

        val contenidoTema = findViewById<EditText>(R.id.editTxt_contenidoTemaForo)

        val btn_CrearTema = findViewById<Button>(R.id.btn_crearTemaForo)

        val btn_Volver = findViewById<Button>(R.id.btn_volverCrearTema)

        //configuracion boton Volver
        btn_Volver.setOnClickListener() {

            val intentVolverForo = Intent(this, Foro::class.java)
            startActivity(intentVolverForo)
        }
    }
}