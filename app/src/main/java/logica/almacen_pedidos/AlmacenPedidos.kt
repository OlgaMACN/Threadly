package logica.almacen_pedidos

import AdaptadorAlmacen
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.BaseActivity
import utiles.funciones.funcionToolbar

class AlmacenPedidos : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: AdaptadorAlmacen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)
        funcionToolbar(this)

        recyclerView = findViewById(R.id.tabla_almacen)
        adaptador = AdaptadorAlmacen(RepositorioPedidos.listaPedidos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adaptador
    }

    override fun onResume() {
        super.onResume()
        adaptador.actualizarLista(RepositorioPedidos.listaPedidos)
    }

}