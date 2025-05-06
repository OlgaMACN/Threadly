package CatalogoHilos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class AgregarHilo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultar_dialog_agregar_hilo)

        val numHilo = findViewById<EditText>(R.id.editTxt_numHiloModificar)

        val nombreHilo = findViewById<EditText>(R.id.editTxt_agregarNombreHilo)

        val btn_Guardar = findViewById<Button>(R.id.btn_GuardarAgregarHilo)

        val btn_Volver = findViewById<Button>(R.id.btn_VolverConsultaHilos)
        //configuracion del boton de volver a catalogo hilos
        btn_Volver.setOnClickListener() {

            val intentVolverCatalogo = Intent(this, CatalogoHilos::class.java)
            startActivity(intentVolverCatalogo)
        }
        //configuracion del boton de guargar el hilo agregado al catalogo
    }
}