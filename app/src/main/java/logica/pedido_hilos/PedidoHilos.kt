package logica.pedido_hilos

import android.app.Dialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import logica.almacen_pedidos.PedidoGuardado
import logica.almacen_pedidos.PedidoSingleton
import logica.grafico_pedido.GraficoPedido
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import java.util.Date
import java.util.Locale

/**
 * Clase principal para la pantalla de creación y edición de pedidos en Threadly.
 *
 * Permite al usuario agregar gráficos a un pedido, editar sus hilos, buscar gráficos,
 * eliminar gráficos del pedido, guardar el pedido actual o realizar el pedido en tiendas externas.
 *
 * La pantalla se adapta para edición si se recibe un pedido ya guardado como extra.
 *
 * @author Olga y Sandra Macías Aragón
 */

private val REQUEST_CODE_GRAFICO_PEDIDO = 1 /* para identificar cada gráfico */

class PedidoHilos : BaseActivity() {

    private lateinit var adaptadorPedido: AdaptadorPedido
    private val listaGraficos = mutableListOf<Grafico>()
    private var pedidoGuardado = false
    private var nombrePedidoEditado: String? = null

    /**
     * Método principal al crear la actividad. Inicializa la vista, carga un pedido si se va a editar
     * y configura los listeners y elementos visuales.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* inicializar el adaptador y configurar el recycler view */
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)
        adaptadorPedido = AdaptadorPedido(listaGraficos,
            onItemClick = { graficoSeleccionado ->
                Log.d("PedidoHilos", "Click en gráfico: ${graficoSeleccionado.nombre}")
                // Lanzamos GraficoPedido propagando sesión y pasando el gráfico
                lanzar(GraficoPedido::class.java) {
                    putExtra("grafico", graficoSeleccionado)
                }
            },
            onEliminarGrafico = { index ->
                dialogoEliminarGrafico(index)
            }
        )
        tablaPedido.adapter = adaptadorPedido


        /* declarar componentes*/
        val btnAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)
        val btnGuardarPedido = findViewById<Button>(R.id.btn_guardarPedidoA)
        val btnRealizarPedido = findViewById<Button>(R.id.btn_realizarPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarGrafico.setOnClickListener { dialogAgregarGrafico() }
        btnRealizarPedido.setOnClickListener { realizarPedido() }
        btnGuardarPedido.setOnClickListener { guardarPedido() }

        /* funciones en continua ejecución durante la pantalla */
        buscadorGrafico()
        actualizarTotalMadejas()
    }

    /**
     * Muestra un buscador para filtrar gráficos por nombre.
     * Permite al usuario escribir y resaltar coincidencias.
     */
    private fun buscadorGrafico() {
        val buscarPedido = findViewById<EditText>(R.id.edTxt_buscadorPedido)
        val btnLupaPedido = findViewById<ImageButton>(R.id.imgBtn_lupaPedido)
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        val txtNoResultadosPedido = findViewById<TextView>(R.id.txtVw_sinResultadosPedido)

        txtNoResultadosPedido.visibility = View.GONE

        btnLupaPedido.setOnClickListener {
            val graficoBuscado = buscarPedido.text.toString().trim().uppercase()
            val coincidencia = listaGraficos.find { it.nombre.uppercase() == graficoBuscado }

            if (coincidencia != null) {
                adaptadorPedido.resaltarGrafico(coincidencia.nombre)
                tablaPedido.visibility = View.VISIBLE
                txtNoResultadosPedido.visibility = View.GONE

                val index = listaGraficos.indexOf(coincidencia)
                tablaPedido.post {
                    tablaPedido.scrollToPosition(index)
                }
            } else {
                tablaPedido.visibility = View.GONE
                txtNoResultadosPedido.visibility = View.VISIBLE
            }
        }
        /* si se borra la búsqueda la tabla vuelve a aparecer */
        buscarPedido.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorPedido.resaltarGrafico(null)
                    adaptadorPedido.actualizarLista(listaGraficos)
                    actualizarTotalMadejas()
                    tablaPedido.visibility = View.VISIBLE
                    txtNoResultadosPedido.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Actualiza el total de madejas mostradas en la interfaz.
     */
    private fun actualizarTotalMadejas() {
        val txtTotal = findViewById<TextView>(R.id.txtVw_madejasTotalPedido)
        val total = adaptadorPedido.obtenerTotalMadejas()
        txtTotal.text = "Total madejas: $total"
    }


    /**
     * Muestra un diálogo para agregar un nuevo gráfico al pedido.
     * Valida que no haya duplicados y que el nombre no esté vacío.
     */
    private fun dialogAgregarGrafico() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_agregar_grafico)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        /* variables del dialog */
        val nombreInput = dialog.findViewById<EditText>(R.id.edTxt_pedirNombreGrafico)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarGrafico)
        val btnCancelar =
            dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog_agregarGrafico)

        btnGuardar.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, introduce un nombre.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            /* verificar si ya existe un gráfico con ese nombre */
            if (listaGraficos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                Toast.makeText(this, "Ya existe un gráfico con ese nombre", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            /* el usuario creará un nuevo gráfico con madejas a cero, hasta que lo edite */
            val nuevoGrafico = Grafico(
                nombre = nombre,
                listaHilos = mutableListOf()
            )

            listaGraficos.add(nuevoGrafico)
            listaGraficos.sortBy { it.nombre.lowercase() }
            adaptadorPedido.actualizarLista(listaGraficos)
            actualizarTotalMadejas()
            dialog.dismiss()
        }
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Muestra un diálogo de confirmación para eliminar un gráfico del pedido.
     *
     * @param index Índice del gráfico en la lista.
     */
    private fun dialogoEliminarGrafico(index: Int) {
        val grafico = listaGraficos[index]
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_eliminar_grafico)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        /* variables del dialog */
        val txtNombreGrafico = dialog.findViewById<TextView>(R.id.txtVw_nombreGrafico)
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_eliminarGrafico)
        val btnVolver =
            dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog)

        /* para visualizar el nombre del gráfico en el dialog */
        txtNombreGrafico.text = grafico.nombre

        btnEliminar.setOnClickListener {
            listaGraficos.removeAt(index)
            adaptadorPedido.actualizarLista(listaGraficos)
            actualizarTotalMadejas()
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Guarda el pedido actual en memoria (singleton) y limpia la lista de gráficos.
     * Si se está editando un pedido, lo sobrescribe.
     */
    private fun guardarPedido() {
        val copiaGraficos = listaGraficos.map { grafico ->
            grafico.copy(
                listaHilos = grafico.listaHilos?.map { it.copy() }?.toMutableList()
                    ?: mutableListOf()
            )
        }

        val nombreFinal = nombrePedidoEditado ?: nombrePedido()
        val nuevoPedido = PedidoGuardado(nombre = nombreFinal, graficos = copiaGraficos)

        // Si estamos editando, reemplazamos el existente
        PedidoSingleton.guardarPedido(nuevoPedido)

        listaGraficos.clear()
        adaptadorPedido.actualizarLista(listaGraficos)
        pedidoGuardado = true
        Toast.makeText(this, "Pedido guardado como $nombreFinal", Toast.LENGTH_SHORT).show()
    }

    /**
     * Genera un nombre único para un nuevo pedido con el formato "Pyyyymmdd" o "Pyyyymmdd(n)".
     *
     * @return Nombre único del pedido.
     */
    private fun nombrePedido(): String {
        val fechaHoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        var baseNombre = "P$fechaHoy"
        var nombreFinal = baseNombre
        var contador = 1

        while (PedidoSingleton.listaPedidos.any { it.nombre == nombreFinal }) {
            nombreFinal = "$baseNombre($contador)"
            contador++
        }
        return nombreFinal
    }

    /**
     * Muestra un diálogo con opciones para realizar el pedido en tiendas externas (Amazon, AliExpress, Temu).
     */
    private fun realizarPedido() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_realizar_pedido)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /* llamada al metodo que centra el dialog en pantalla */
        ajustarDialog(dialog)

        dialog.setCancelable(false)

        /* variables del dialog */
        val btnAmazon = dialog.findViewById<ImageButton>(R.id.btn_amazon)
        val btnAliExpress = dialog.findViewById<ImageButton>(R.id.btn_aliexpress)
        val btnTemu = dialog.findViewById<ImageButton>(R.id.btn_temu)
        val btnVolver =
            dialog.findViewById<Button>(R.id.btn_volver_pedido_dialog)

        btnAmazon.setOnClickListener {
            abrirTienda("com.amazon.mShop.android.shopping", "https://www.amazon.es/")
            dialog.dismiss()
        }

        btnAliExpress.setOnClickListener {
            abrirTienda("com.alibaba.aliexpresshd", "https://www.aliexpress.com/")
            dialog.dismiss()
        }

        btnTemu.setOnClickListener {
            abrirTienda("com.einnovation.temu", "https://www.temu.com/")
            dialog.dismiss()
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Lanza la app de una tienda si está instalada, o su web si no lo está.
     *
     * @param paquete Nombre del paquete de la aplicación.
     * @param urlWeb URL de la tienda online.
     */
    private fun abrirTienda(paquete: String, urlWeb: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(paquete)
            if (intent != null) {
                startActivity(intent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWeb))
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al redirigir a la tienda.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Recibe un gráfico actualizado desde la actividad GraficoPedido y actualiza su información.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GRAFICO_PEDIDO && resultCode == RESULT_OK && data != null) {
            val updatedGrafico = data.getSerializableExtra("grafico") as? Grafico ?: return
            val position = data.getIntExtra("position", -1)
            if (position in listaGraficos.indices) {
                /* reemplaza el gráfico con su versión actualizada */
                listaGraficos[position] = updatedGrafico
                /* se notifica al adaptador */
                adaptadorPedido.actualizarLista(listaGraficos)
                /* y se actualiza */
                actualizarTotalMadejas()
            }
        }
    }

    /**
     * Muestra el diálogo de guardar pedido si hay cambios pendientes.
     *
     * @param salirDespues Indica si se debe cerrar la pantalla tras guardar o cancelar.
     * @param destino Acción opcional a ejecutar si no se cancela.
     */
    private fun dialogGuardarPedido(salirDespues: Boolean = true, destino: (() -> Unit)? = null) {
        if (pedidoGuardado || listaGraficos.isEmpty()) {
            if (destino != null) {
                destino()
            } else if (salirDespues) {
                finish()
            }
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pedido_dialog_guardar_pedido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarPedido)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverSinGuardar)

        btnGuardar.setOnClickListener {
            guardarPedido()
            dialog.dismiss()
            if (destino != null) {
                destino()
            } else if (salirDespues) {
                finish()
            }
        }

        btnVolver.setOnClickListener {
            dialog.dismiss()
            if (destino != null) {
                destino()
            } else if (salirDespues) {
                finish()
            }
        }

        dialog.show()
    }
}