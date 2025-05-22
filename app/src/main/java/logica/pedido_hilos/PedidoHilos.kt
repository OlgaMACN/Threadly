package logica.pedido_hilos

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import logica.grafico_pedido.GraficoPedido
import utiles.BaseActivity
import utiles.funciones.ajustarDialog
import utiles.ExportadorCSV
import utiles.funciones.funcionToolbar

private val REQUEST_CODE_GRAFICO_PEDIDO = 1 /* para identificar cada gráfico */

class PedidoHilos : BaseActivity() {

    private lateinit var adaptadorPedido: AdaptadorPedido
    private val listaGraficos = mutableListOf<Grafico>()

    /* para descargar el pedido */
    companion object {
        private const val REQUEST_CODE_WRITE = 1001
    }

    private lateinit var btnDescargarPedido: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* inicializar el adaptador y configurar el recycler view */
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)

        /* inicializo el adaptador manejando los clics sobre la tabla */
        adaptadorPedido = AdaptadorPedido(listaGraficos, onItemClick = { graficoSeleccionado ->
            val position = listaGraficos.indexOf(graficoSeleccionado)
            val intent = Intent(this, GraficoPedido::class.java).apply {
                putExtra("grafico", graficoSeleccionado)
                putExtra("position", position)
            }
            startActivityForResult(intent, REQUEST_CODE_GRAFICO_PEDIDO)
        }, onLongClick = { index ->
            dialogoEliminarGrafico(index)
        })
        /* asignación del adaptador */
        tablaPedido.adapter = adaptadorPedido

        /* declarar componentes*/
        val btnAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)
        val btnGuardarPedido = findViewById<Button>(R.id.btn_guardarPedido)
        val btnRealizarPedido = findViewById<Button>(R.id.btn_realizarPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarGrafico.setOnClickListener { dialogAgregarGrafico() }
        btnRealizarPedido.setOnClickListener { realizarPedido() }

        /* descargar pedido: al pulsar se comprueban permisos y se exporta */
        btnDescargarPedido.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    /* se pide permiso al usuario */
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_WRITE
                    )
                } else {
                    exportarPedidoComoCSV()
                }
            } else {
                /* si la versión de android es +10 no hace falta pedir permiso */
                exportarPedidoComoCSV()
            }
        }
        /* funciones en continua ejecución durante la pantalla */
        buscadorGrafico()
        actualizarTotalMadejas()
    }

    /* ahora en función de si el usuario ha dado permiso para la descarga o no... */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permisos: Array<out String>,
        concederPermisos: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permisos, concederPermisos)
        if (requestCode == REQUEST_CODE_WRITE &&
            concederPermisos.isNotEmpty() &&
            concederPermisos[0] == PackageManager.PERMISSION_GRANTED
        ) {
            exportarPedidoComoCSV() /* es descarga */
        } else {
            Toast.makeText(
                this,
                "Permiso denegado :(, no se puede guardar el pedido.", /* o se interrumpe */
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /* método para guardar los hilos con sus madejas */
    private fun exportarPedidoComoCSV() {
        // Agrupar los hilos con suma de madejas
        val hilosAgrupados = mutableMapOf<String, Int>()
        for (grafico in listaGraficos) {
            for (hiloGrafico in grafico.listaHilos) {
                val nombre = hiloGrafico.hilo.trim()
                val madejas = hiloGrafico.madejas
                hilosAgrupados[nombre] = hilosAgrupados.getOrDefault(nombre, 0) + madejas
            }
        }

        // Usar la utilidad
        ExportadorCSV.exportarPedido(this, hilosAgrupados)
    }


    /* buscar un gráfico dentro del pedido */
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

    /* sumar el total de madejas del pedido */
    private fun actualizarTotalMadejas() {
        val txtTotal = findViewById<TextView>(R.id.txtVw_madejasTotalPedido)
        val total = adaptadorPedido.obtenerTotalMadejas()
        txtTotal.text = "Total madejas: $total"
    }

    /* agregar un gráfico al pedido */
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

    /* eliminar un gráfico del pedido */
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

    /* realizar pedido */
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

    /* función auxiliar para abrir la aplicación o la web de cada tienda */
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

    /* recoger el dato del total de madejas */
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
}