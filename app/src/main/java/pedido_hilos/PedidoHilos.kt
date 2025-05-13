package pedido_hilos

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R

class PedidoHilos : AppCompatActivity() {

    private lateinit var adaptadorPedido: AdaptadorPedido
    private val listaGraficos = mutableListOf<Grafico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido_aa_principal)

        /* llamada a la función para usar el toolbar */
        Toolbar.funcionToolbar(this)

        /* inicializar el adaptador */
        val tablaPedido = findViewById<RecyclerView>(R.id.tabla_pedido)
        tablaPedido.layoutManager = LinearLayoutManager(this)

        /* inicializo el adaptador */
        val recyclerView = findViewById<RecyclerView>(R.id.tabla_pedido)
        adaptadorPedido = AdaptadorPedido(listaGraficos,
            onItemClick = { /* manejar click si quieres */ },
            onLongClick = { index ->
                dialogoEliminarGrafico(index) /* no hace falta declarar un botón aparte, ya se elimina el gráfico manteniendo pulsado */
            }
        )
        recyclerView.adapter = adaptadorPedido
        recyclerView.layoutManager = LinearLayoutManager(this)

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
        val dialogView = layoutInflater.inflate(R.layout.pedido_dialog_agregar_grafico, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val nombreInput = dialogView.findViewById<EditText>(R.id.edTxt_pedirNombreGrafico)
        val countTelaInput = dialogView.findViewById<EditText>(R.id.edTxt_pedirCountTela)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btn_guardarGrafico)
        val btnCancelar =
            dialogView.findViewById<Button>(R.id.btn_volver_pedido_dialog_agregarGrafico)

        btnGuardar.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val countTela = countTelaInput.text.toString().toIntOrNull()

            if (nombre.isEmpty() || countTela == null) {
                Toast.makeText(
                    this,
                    "Por favor completa todos los campos correctamente.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            /* verificar que el count de tela esté entre los permitidos */
            val countsPermitidos = listOf(14, 16, 18, 20, 25)
            if (countTela !in countsPermitidos) {
                Toast.makeText(
                    this,
                    "El count de tela debe ser 14, 16, 18, 20 o 25.",
                    Toast.LENGTH_SHORT
                ).show()
                countTelaInput.text.clear()  /* borra el campo para que lo escriba otra vez */
                return@setOnClickListener
            }

            /* verificar si ya existe un gráfico con ese nombre */
            if (listaGraficos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                Toast.makeText(this, "Ya existe un gráfico con ese nombre", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val nuevoGrafico = Grafico(nombre, countTela)
            listaGraficos.add(nuevoGrafico)
            listaGraficos.sortBy { it.nombre.lowercase() } /* para mostrar la tabla por orden alfabético */
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
        val dialogView = layoutInflater.inflate(R.layout.pedido_dialog_eliminar_grafico, null)

        val txtNombreGrafico = dialogView.findViewById<TextView>(R.id.txtVw_nombreGrafico)
        val btnEliminar = dialogView.findViewById<Button>(R.id.btn_eliminarGrafico)
        val btnVolver =
            dialogView.findViewById<Button>(R.id.btn_volver_pedido_dialog)

        /* para visualizar el nombre del gráfico en el dialog */
        txtNombreGrafico.text = grafico.nombre

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

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

    /* descargar pedido */ //TODO queda probarlo cuando grafico_pedido funcione bien
    private fun descargarPedido() {
        if (!solicitarPermisoAlmacenamiento()) return

        if (listaGraficos.isEmpty()) {
            Toast.makeText(this, "No hay gráficos en el pedido para exportar.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        /* mapa para acumular las madejas por cada hilo */
        val madejasPorHilo = mutableMapOf<String, Int>()
        for (grafico in listaGraficos) {
            for (hilo in grafico.listaHilos) {
                val totalAnterior = madejasPorHilo[hilo.hilo] ?: 0
                madejasPorHilo[hilo.hilo] = totalAnterior + hilo.madejas
            }
        }

        /* conseguir la fecha para el título del fichero */
        val timestamp =
            java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm", java.util.Locale.getDefault())
                .format(java.util.Date())
        val fileName = "Pedido_$timestamp.csv"
        val downloadsDir =
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = java.io.File(downloadsDir, fileName)

        try {
            file.printWriter().use { out ->
                out.println("Hilo,Madejas")
                madejasPorHilo.forEach { (identificador, madejas) ->
                    out.println("$identificador,$madejas")
                }
            }

            Toast.makeText(this, "Pedido guardado en Descargas :D", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar el pedido: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }


    private fun solicitarPermisoAlmacenamiento(): Boolean { /* para poder descargar el archivo en el teléfono */
        val permiso = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (checkSelfPermission(permiso) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permiso), 100)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            descargarPedido()
        } else {
            Toast.makeText(
                this,
                "No nos has dado permiso para guardar el archivo :(.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    /* realizar pedido */
    @SuppressLint("MissingInflatedId")
    private fun realizarPedido() {
        val dialogView = layoutInflater.inflate(R.layout.pedido_dialog_realizar_pedido, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        /* declaración de botones*/
        val btnAmazon = dialogView.findViewById<ImageButton>(R.id.btn_amazon)
        val btnAliExpress = dialogView.findViewById<ImageButton>(R.id.btn_aliexpress)
        val btnTemu = dialogView.findViewById<ImageButton>(R.id.btn_temu)
        val btnVolver =
            dialogView.findViewById<Button>(R.id.btn_volver_pedido_dialog)

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