package PantallaInicio


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import login.LoginUserExiste

class DatosPersonales : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_datos_personales)

        //declaramos todos los botones de esta pantalla
        val btn_modificarDatos = findViewById<Button>(R.id.btn_ModificarDatos)

        val btn_cerrarSesion = findViewById<Button>(R.id.btn_CerrarSesion)

        val btn_eliminarCuenta = findViewById<Button>(R.id.btn_EliminarCuenta)

        val btn_volverPantallaInicio = findViewById<Button>(R.id.btn_VolverDatosPersonales)

        //caso 1. Modificar datos
        btn_modificarDatos.setOnClickListener() {

            val intentModificarDatos = Intent (this, ModificarDatos::class.java)
            startActivity(intentModificarDatos)
        }

        //caso 2. Cerrar sesión
        btn_cerrarSesion.setOnClickListener() {

            val intentCerrarSesion = Intent(this, LoginUserExiste::class.java)
            startActivity(intentCerrarSesion)
            finish() // TODO CON DOBLE CLICK HACIA ATRÁS SE VUELVE A PANTALLA PRINCIPAL !!!!
        }

        //caso 3. Eliminar Cuenta
//        btn_eliminarCuenta.setOnClickListener() {
//
//            val intentEliminarCuenta = Intent(this, ...)
//        }

        //caso 4. Volver a pantalla de inicio
        btn_volverPantallaInicio.setOnClickListener() {

            val intentVolver = Intent(this, PantallaPrincipal::class.java)
            startActivity(intentVolver)
        }
    }
}