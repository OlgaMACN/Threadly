package logica.almacen_pedidos

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.funciones.exportarPedidoCSV
import utiles.funciones.funcionToolbar

/**
 * Clase que representa la pantalla de almacenamiento de pedidos en la aplicación Threadly.
 *
 * Permite a los usuarios:
 * - Visualizar todos los pedidos realizados.
 * - Buscar pedidos por nombre.
 * - Descargar un pedido como archivo CSV (Android 10 o superior).
 * - Marcar un pedido como realizado y actualizar el stock personal.
 * - Eliminar pedidos con confirmación mediante diálogo.
 *
 * Esta clase extiende de [BaseActivity] que proporciona una barra de herramientas común
 * y otras utilidades generales para actividades.
 *
 * @author Olga y Sandra Macías Aragón
 */
class AlmacenPedidos : BaseActivity() {

    /* recyclerView para mostrar la lista de pedidos almacenados */
    private lateinit var tablaAlmacen: RecyclerView

    /* adaptador personalizado que gestiona la vista y acciones de cada pedido */
    private lateinit var adaptador: AdaptadorAlmacen

    /**
     * Método llamado cuando se crea la actividad.
     * Configura la interfaz, el adaptador y funcionalidades de búsqueda, descarga y procesamiento de pedidos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.almacen_aa_pedidos)

        /* configura la barra de herramientas */
        funcionToolbar(this)

        /* inicializa el RecyclerView desde el layout */
        tablaAlmacen = findViewById(R.id.tabla_almacen)

        /* crea el adaptador con acciones de descarga y marcar como realizado */
        adaptador = AdaptadorAlmacen(
            PedidoSingleton.listaPedidos,
            onDescargarClick = { pedido ->
                /* verifica si la versión del sistema permite exportar archivos */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resultado = exportarPedidoCSV(this, pedido)
                    /* muestra mensaje según el resultado */
                    if (resultado) {
                        Toast.makeText(
                            this,
                            "Pedido guardado en Descargas/Threadly",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "Error al descargar :(", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    /* dispositivo no compatible con exportación */
                    Toast.makeText(
                        this,
                        "Exportación disponible solo en Android 10 o superior",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onPedidoRealizadoClick = { pedido ->
                /* añade las madejas pedidas al stock del usuario */
                pedido.graficos.forEach { grafico ->
                    grafico.listaHilos.forEach { hiloGrafico ->
                        //StockSingleton.agregarMadejas(hiloGrafico.hilo, hiloGrafico.madejas)
                    }
                }
                pedido.realizado = true /* marca el pedido como realizado */
                Toast.makeText(this, "Pedido realizado y stock actualizado", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        /* configura el RecyclerView con un layout lineal y asigna el adaptador */
        tablaAlmacen.layoutManager = LinearLayoutManager(this)
        tablaAlmacen.adapter = adaptador

        /* imprime en log la cantidad inicial de pedidos y sus nombres (para depuración) */
        Log.d("AlmacenPedidos", "Cantidad de pedidos inicial: ${PedidoSingleton.listaPedidos.size}")
        PedidoSingleton.listaPedidos.forEach {
            Log.d("AlmacenPedidos", "Pedido: ${it.nombre}")
        }

        /* activa el buscador de pedidos */
        buscadorPedido()
    }

    /**
     * Método ejecutado al reanudar la actividad.
     * Refresca la lista del adaptador con los pedidos actuales.
     */
    override fun onResume() {
        super.onResume()
        adaptador.actualizarLista(PedidoSingleton.listaPedidos)
    }

    /**
     * Habilita la funcionalidad de búsqueda de pedidos por nombre.
     * También permite restaurar la lista completa si el campo se borra.
     */
    private fun buscadorPedido() {
        val edtBuscador = findViewById<EditText>(R.id.edTxt_buscadorAlmacen)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaAlmacen)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultadosAlmacen)

        txtNoResultados.visibility = View.GONE // Oculta el mensaje al iniciar

        btnLupa.setOnClickListener {
            val texto = edtBuscador.text.toString().trim().uppercase()
            Log.d("Buscador", "Texto introducido: '$texto'")

            /* si el texto está vacío, muestra todos los pedidos */
            if (texto.isEmpty()) {
                adaptador.actualizarLista(PedidoSingleton.listaPedidos)
                txtNoResultados.visibility = View.GONE
                return@setOnClickListener
            }

            /* filtra los pedidos por nombre (ignorando mayúsculas/minúsculas) */
            val listaFiltrada = PedidoSingleton.listaPedidos.filter {
                it.nombre.uppercase().contains(texto)
            }

            /* actualiza la lista con los resultados encontrados */
            if (listaFiltrada.isNotEmpty()) {
                adaptador.actualizarLista(listaFiltrada)
                txtNoResultados.visibility = View.GONE
            } else {
                /* si no hay coincidencias, muestra mensaje */
                adaptador.actualizarLista(emptyList())
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        /* lListener para restaurar la lista si el usuario borra el contenido del buscador */
        edtBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptador.actualizarLista(PedidoSingleton.listaPedidos)
                    txtNoResultados.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Muestra un diálogo personalizado para confirmar la eliminación de un pedido.
     * Resalta el nombre del pedido en rojo dentro del texto del diálogo.
     *
     * @param posicion Índice del pedido en la lista que se desea eliminar.
     */
    fun dialogEliminarPedido(posicion: Int) {
        /* crea y configura el diálogo de eliminación */
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.almacen_dialog_eliminar_pedido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false) /* impide cerrar el diálogo sin pulsar un botón */

        /* obtiene referencias a los elementos del diálogo */
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarPedido)
        val txtMensaje = dialog.findViewById<TextView>(R.id.txtVw_confirmarEliminarPedido)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_almacen)

        val pedido = PedidoSingleton.listaPedidos[posicion]
        val nombrePedido = pedido.nombre
        val textoOriginal = getString(R.string.confirmarEliminarPedido)
        val textoConPedido = textoOriginal.replace("%s", nombrePedido)

        /* crea un texto enriquecido (Spannable) para destacar el nombre del pedido */
        val spannable = SpannableString(textoConPedido)
        val start = textoConPedido.indexOf(nombrePedido)
        val end = start + nombrePedido.length

        if (start != -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        /* asigna el texto enriquecido al mensaje del diálogo */
        txtMensaje.text = spannable

        /* si el usuario cancela, se cierra el diálogo */
        btnVolver.setOnClickListener { dialog.dismiss() }

        /* si el usuario confirma, elimina el pedido, actualiza la lista y muestra mensaje */
        btnEliminar.setOnClickListener {
            PedidoSingleton.listaPedidos.removeAt(posicion)
            adaptador.actualizarLista(PedidoSingleton.listaPedidos)
            Toast.makeText(this, "Pedido '$nombrePedido' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        /* muestra el diálogo al usuario */
        dialog.show()
    }
}
