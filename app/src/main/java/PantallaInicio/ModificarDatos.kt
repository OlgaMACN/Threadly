package PantallaInicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class ModificarDatos : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_modificar_datos)

        //declaramos botones de esta pantalla
        val btnGuardarCambios = findViewById<Button>(R.id.btn_GuardarCambios)

        val btnVolverDatosPersonales = findViewById<Button>(R.id.btn_VolverModificarDatos)

        //configuración botón 'guardar cambios'
        btnGuardarCambios.setOnClickListener() {


        }

        //configuración botón 'volver'
        btnVolverDatosPersonales.setOnClickListener() {

            val volverDatosPersonales = Intent(this, DatosPersonales::class.java)
            startActivity(volverDatosPersonales)
        }
    }
}