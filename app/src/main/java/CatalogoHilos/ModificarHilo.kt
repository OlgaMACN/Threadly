package CatalogoHilos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class ModificarHilo : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultar_dialog_modificar)

        //declaramos elementos de esta pantalla
        val numHiloModificar = findViewById<EditText>(R.id.editTxt_numHiloModificar)

        val campoNumHilo = findViewById<CheckBox>(R.id.chkBx_numHilo)

        val campoNombreHilo = findViewById<CheckBox>(R.id.chkBx_nombreHilo)

        val btn_Siguiente = findViewById<Button>(R.id.btn_SiguienteModificar)

        val btn_Volver = findViewById<Button>(R.id.btn_VolverModificar)

        //configuracion boton siguiente
        btn_Siguiente.setOnClickListener() {

            val intentSiguiente = Intent (this, ModificarHiloFinal::class.java)
            startActivity(intentSiguiente)
        }
        //configuracion boton volver
        btn_Volver.setOnClickListener() {

            val intentVolverCatalogo = Intent (this, CatalogoHilos::class.java)
            startActivity(intentVolverCatalogo)
        }

    }

}