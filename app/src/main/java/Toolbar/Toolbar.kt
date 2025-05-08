package Toolbar

import CatalogoHilos.CatalogoHilos
import Foro.Foro
import PantallaInicio.PantallaPrincipal
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import pedido_hilos.PedidoHilos_A
import stock_personal.StockPersonal

class Toolbar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_layout)

        /* inicialización de botones del toolbar */
        val btn_inicio = findViewById<ImageButton>(R.id.botonInicio)
        val btn_catalogo = findViewById<ImageButton>(R.id.botonCatalogo)
        val btn_stock = findViewById<ImageButton>(R.id.botonStock)
        val btn_foro = findViewById<ImageButton>(R.id.botonForo)
        val btn_pedido = findViewById<ImageButton>(R.id.botonPedido)

        /* configuración botón inicio */
        btn_inicio.setOnClickListener() {
            val intentInicio = Intent(this, PantallaPrincipal::class.java)
            startActivity(intentInicio)
        }

        /* configuración botón catálogo */
        btn_catalogo.setOnClickListener() {
            val intentCatalogo = Intent(this, CatalogoHilos::class.java)
            startActivity(intentCatalogo)
        }

        /* configuración botón stock */
        btn_stock.setOnClickListener() {
            val intentStock = Intent(this, StockPersonal::class.java)
            startActivity(intentStock)
        }

        /* configuración botón foro */
        btn_foro.setOnClickListener() {
            val intentForo = Intent(this, Foro::class.java)
            startActivity(intentForo)
        }

        /* configuración botón pedido */
        btn_pedido.setOnClickListener() {
            val intentPedido = Intent(this, PedidoHilos_A::class.java)
        }
    }
}