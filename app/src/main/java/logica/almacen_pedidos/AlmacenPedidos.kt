package logica.almacen_pedidos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R
import utiles.funcionToolbar

class AlmacenPedidos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */
    }
}