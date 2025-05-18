package logica.PantallaInicio


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import logica.login.LoginUserExiste
import logica.login.LoginUserNoExiste

class DatosPersonales : AppCompatActivity() {

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

            val intentModificarDatos = Intent(this, ModificarDatos::class.java)
            startActivity(intentModificarDatos)
        }

        //caso 2. Cerrar sesión
        btn_cerrarSesion.setOnClickListener() {

            val intentCerrarSesion = Intent(this, LoginUserExiste::class.java)
            startActivity(intentCerrarSesion)
            finish()
            // TODO CON DOBLE CLICK HACIA ATRÁS SE VUELVE A PANTALLA PRINCIPAL !!!!
            //TODO habria que poner el finish en la pantalla de Login
        }

        //caso 3. Eliminar Cuenta
        btn_eliminarCuenta.setOnClickListener() {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pantalla_dialog_eliminar_cuenta)

            /* se oscurece el fondo y queda súper chulo */
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            /* ancho y alto para configurar el tamaño independientemente del layout */
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            /* con setCancelable se consigue que no se cierre el dialogo si el user clica fuera de él */
            dialog.setCancelable(false)

            val btnArrepentido = dialog.findViewById<Button>(R.id.btn_Arrepentimiento)
            val btnEliminarCuentaThreadly = dialog.findViewById<Button>(R.id.btn_EliminarCuentaThreadly)

            btnArrepentido.setOnClickListener() {

                dialog.dismiss()

                Toast.makeText(
                    this,
                    "Ufff, menudo susto...que bien que te quedes \uD83E\uDD70",
                    Toast.LENGTH_LONG
                ).show()
            }

            btnEliminarCuentaThreadly.setOnClickListener() {

                val intentLogin = Intent(this, LoginUserNoExiste::class.java)
                startActivity(intentLogin)

                Toast.makeText(
                    this,
                    "Se ha eliminado tu cuenta, hasta pronto...\uD83D\uDE22",
                    Toast.LENGTH_LONG
                ).show()
            }

            dialog.show()

        }

        //caso 4. Volver a pantalla de inicio
        btn_volverPantallaInicio.setOnClickListener() {

            val intentVolver = Intent(this, PantallaPrincipal::class.java)
            startActivity(intentVolver)
        }


    }


}