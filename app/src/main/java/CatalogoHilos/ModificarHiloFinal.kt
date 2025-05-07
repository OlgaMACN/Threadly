package CatalogoHilos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class ModificarHiloFinal : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultar_dialog_modificar_final)

        //declaramos elementos de esta pantalla
        val numHiloModificar = findViewById<EditText>(R.id.edtxt_numHiloModificar)

        val nombreHiloModificar = findViewById<EditText>(R.id.edtxt_nombreHiloModificar)

        val btn_Guardar = findViewById<Button>(R.id.btn_guardarModificarHiloFinal)

        val btn_Volver = findViewById<Button>(R.id.btn_volver_modificarHiloFinal)

        //configuracion boton volver
        btn_Volver.setOnClickListener() {

            val intentVolverModificar = Intent(this, ModificarHilo::class.java)
            startActivity(intentVolverModificar)
        }


    }
}