package Toolbar

import CatalogoHilos.CatalogoHilos
import Foro.Foro
import PantallaInicio.PantallaPrincipal
import StockPersonal.StockPersonal
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class Toolbar : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_layout)

        val btn_inicio = findViewById<ImageButton>(R.id.botonInicio)

        val btn_catalogo = findViewById<ImageButton>(R.id.botonCatalogo)

        val btn_stock = findViewById<ImageButton>(R.id.botonStock)

        val btn_foro = findViewById<ImageButton>(R.id.botonForo)

        val btn_pedido = findViewById<ImageButton>(R.id.botonPedido)

        //configuracion boton inicio
        btn_inicio.setOnClickListener() {

            val intentInicio = Intent(this, PantallaPrincipal::class.java)
            startActivity(intentInicio)
        }
        //configuracion boton catalogo
        btn_catalogo.setOnClickListener() {

            val intentCatalogo = Intent(this, CatalogoHilos::class.java)
            startActivity(intentCatalogo)
        }
        //configuracion boton stock
        btn_stock.setOnClickListener() {

            val intentStock = Intent(this, StockPersonal::class.java)
            startActivity(intentStock)
        }
        //configuracion boton foro
        btn_foro.setOnClickListener() {

            val intentForo = Intent(this, Foro::class.java)
            startActivity(intentForo)
        }
        //configuracion boton pedido
//        btn_pedido.setOnClickListener() {
//
//            val intentPedido = Intent (this, Pedi)
//        }
    }

}