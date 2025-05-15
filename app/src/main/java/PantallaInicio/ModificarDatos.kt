package PantallaInicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class ModificarDatos : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_modificar_datos)

        //declaramos botones de esta pantalla
        val btnGuardarCambios = findViewById<Button>(R.id.btn_GuardarCambios)

        val btnVolverDatosPersonales = findViewById<Button>(R.id.btn_VolverModificarDatos)

        //configuración botón 'guardar cambios'
        btnGuardarCambios.setOnClickListener() {

            val avatarUno = findViewById<ImageView>(R.id.imgVw_avatar1)
            val avatarDos = findViewById<ImageView>(R.id.imgVw_avatar2)
            val avatarTres = findViewById<ImageView>(R.id.imgVw_avatar3)
            val avatarCuatro = findViewById<ImageView>(R.id.imgVw_avatar4)
            val avatarCinco = findViewById<ImageView>(R.id.imgVw_avatar5)
            val avatarSeis = findViewById<ImageView>(R.id.imgVw_avatar6)

            val inputNombreNuevo = findViewById<EditText>(R.id.txtVw_contenidoCambioNombre)

            //TODO al escoger una imagen, ¿dialog de si quiere hacer el cambio con Aceptar o Volver y despues guardar cambios?
            //TODO ¿con el nombre otro dialog para verificar cambio?


        }

        //configuración botón 'volver'
        btnVolverDatosPersonales.setOnClickListener() {

            val volverDatosPersonales = Intent(this, DatosPersonales::class.java)
            startActivity(volverDatosPersonales)
        }
    }
}