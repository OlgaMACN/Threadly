package logica.almacen_pedidos

import android.os.Bundle
import com.threadly.R
import utiles.BaseActivity
import utiles.funciones.funcionToolbar

class AlmacenPedidos : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */
    }
}