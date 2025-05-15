package pedido_hilos

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import grafico_pedido.GraficoPedido
import ui_utils.ajustarDialog
import utiles.funcionToolbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val REQUEST_CODE_GRAFICO_PEDIDO = 1 /* para identificar cada pedido */

class PedidoHilos : AppCompatActivity() {

    private lateinit var adaptadorPedido: AdaptadorPedido
    private val listaGraficos = mutableListOf<Grafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)
        funcionToolbar(this) /* llamada a la función para usar el toolbar */

        /* inicializar el adaptador y configurar el recycler view */
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)

        /* inicializo el adaptador manejando los clics sobre la tabla */
        adaptadorPedido = AdaptadorPedido(listaGraficos, onItemClick = { graficoSeleccionado ->
            val intent = Intent(this, GraficoPedido::class.java)
            intent.putExtra("grafico", graficoSeleccionado)
            startActivity(intent)
        }, onLongClick = { index ->
            dialogoEliminarGrafico(index)
        })
        /* asignación del adaptador */
        tablaPedido.adapter = adaptadorPedido

        /* declarar componentes*/
        val btnAgregarGrafico = findViewById<Button>(R.id.btn_agregarGraficoPedido)
        val btnDescargarPedido = findViewById<Button>(R.id.btn_descargarPedido)
        val btnRealizarPedido = findViewById<Button>(R.id.btn_realizarPedido)

        /* cuando se pulsan se llevan a cabo sus acciones */
        btnAgregarGrafico.setOnClickListener { dialogAgregarGrafico() }
        btnDescargarPedido.setOnClickListener { descargarPedido() }
        btnRealizarPedido.setOnClickListener { realizarPedido() }

        /* funciones en continua ejecución durante la pantalla */
        buscadorGrafico()
        actualizarTotalMadejas()
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

    /* TODO descargarPedido no descarga nada, tengo que arreglarlo */
    /* descargar pedido */
    private fun descargarPedido() {
        /* antes de descargar nada, se comprueba que el contenido de la lista no esté vacío */
        if (listaGraficos.isEmpty()) {
            Toast.makeText(this, "No hay gráficos para descargar.", Toast.LENGTH_SHORT).show()
            return
        }
        /* fecha y hora para el nombre del archivo */
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val timestamp = formatter.format(Date())
        val fileName = "Pedido_$timestamp.csv"
        val file = File(getExternalFilesDir(null), fileName)

        /* mapa para agrupar las madejas por hilo */
        val madejasPorHilo = mutableMapOf<String, Int>()

        for (grafico in listaGraficos) {
            for (hilo in grafico.listaHilos) {
                val hiloNumero = hilo.hilo
                val madejas = hilo.madejas

                madejasPorHilo[hiloNumero] = madejasPorHilo.getOrDefault(hiloNumero, 0) + madejas
            }
        }

        /* construir el fichero a partir de los hilos y las madejas */
        val contenido = StringBuilder()
        contenido.append("Hilo,Madejas\n")
        for ((hilo, totalMadejas) in madejasPorHilo) {
            contenido.append("$hilo,$totalMadejas\n")
        }

        /* escribir el archivo e informar de su descarga */
        try {
            file.writeText(contenido.toString())
            Toast.makeText(this, "Archivo guardado: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error al guardar el archivo: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }

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
}