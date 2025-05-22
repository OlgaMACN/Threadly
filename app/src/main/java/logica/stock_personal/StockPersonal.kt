package logica.stock_personal

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import logica.stock_personal.StockSingleton.esPrimeraVez
import logica.stock_personal.StockSingleton.marcarPrimeraVez
import utiles.BaseActivity
import utiles.funciones.LeerXMLCodigo
import utiles.funciones.ValidarFormatoHilos
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

class StockPersonal : BaseActivity() {

    private lateinit var tablaStock: RecyclerView
    private lateinit var adaptadorStock: AdaptadorStock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)

        funcionToolbar(this)

        tablaStock = findViewById(R.id.tabla_stock)
        adaptadorStock = AdaptadorStock(StockSingleton.listaStock, ::dialogEliminarHilo)
        tablaStock.layoutManager = LinearLayoutManager(this)
        tablaStock.adapter = adaptadorStock

        if (esPrimeraVez(this)) {
            val listaInicial = LeerXMLCodigo(this, R.raw.catalogo_hilos)
            StockSingleton.listaStock.addAll(listaInicial)
            adaptadorStock.actualizarLista(StockSingleton.listaStock)
            marcarPrimeraVez(this)
        }

        StockSingleton.inicializarStockSiNecesario(this)

        val btnAgregarHilo = findViewById<Button>(R.id.btn_agregarHiloStk)
        val btnAgregarMadeja = findViewById<Button>(R.id.btn_agregarMadejaStk)
        val btnEliminarMadeja = findViewById<Button>(R.id.btn_eliminarMadejaStk)

        btnAgregarHilo.setOnClickListener { dialogAgregarHilo() }
        btnAgregarMadeja.setOnClickListener { dialogAgregarMadeja() }
        btnEliminarMadeja.setOnClickListener { dialogEliminarMadeja() }

        buscadorHilo()
    }

    private fun buscadorHilo() {
        val hiloBuscado = findViewById<EditText>(R.id.edTxt_buscadorHilo)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaStock)
        val txtNoResultados = findViewById<TextView>(R.id.txtVw_sinResultados)

        txtNoResultados.visibility = View.GONE

        btnLupa.setOnClickListener {
            val texto = hiloBuscado.text.toString().trim().uppercase()
            val coincidencia = StockSingleton.listaStock.find { it.hiloId == texto }

            if (coincidencia != null) {
                adaptadorStock.resaltarHilo(coincidencia.hiloId)
                adaptadorStock.actualizarLista(StockSingleton.listaStock)
                tablaStock.visibility = View.VISIBLE
                txtNoResultados.visibility = View.GONE
                tablaStock.scrollToPosition(StockSingleton.listaStock.indexOf(coincidencia))
            } else {
                tablaStock.visibility = View.GONE
                txtNoResultados.visibility = View.VISIBLE
            }
        }

        hiloBuscado.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorStock.resaltarHilo(null)
                    adaptadorStock.actualizarLista(StockSingleton.listaStock)
                    tablaStock.visibility = View.VISIBLE
                    txtNoResultados.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun dialogAgregarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inputMadejas = dialog.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_agregarHilo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_botonAgregarHiloStk)

        btnVolver.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val hilo = inputHilo.text.toString().uppercase().trim()
            val madejasString = inputMadejas.text.toString().trim()

            if (hilo.isEmpty() || madejasString.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!ValidarFormatoHilos.formatoValidoHilo(hilo)) {
                Toast.makeText(this, "Formato inválido: solo letras y números", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val madejas = madejasString.toIntOrNull()
            if (madejas == null || madejas < 0) {
                Toast.makeText(this, "Solo números enteros positivos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (StockSingleton.listaStock.any { it.hiloId == hilo }) {
                Toast.makeText(this, "El hilo '$hilo' ya existe", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val hiloNuevo = HiloStock(hilo, madejas)
            StockSingleton.listaStock.add(hiloNuevo)
            StockSingleton.listaStock = ordenarHilos(StockSingleton.listaStock) { it.hiloId }.toMutableList()
            adaptadorStock.actualizarLista(StockSingleton.listaStock)

            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogAgregarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inputHilo = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk_hilo)
        val inputCantidad = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk)
        val txtMadejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_guardarMadejaStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)

        inputCantidad.isEnabled = false
        btnGuardar.isEnabled = false

        inputHilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hilo = s.toString().uppercase().trim()
                val item = StockSingleton.listaStock.find { it.hiloId == hilo }

                if (item != null) {
                    txtMadejasActuales.text = "Madejas actuales: ${item.madejas}"
                    inputCantidad.isEnabled = true
                    btnGuardar.isEnabled = true
                } else {
                    txtMadejasActuales.text = "Madejas actuales: -"
                    inputCantidad.isEnabled = false
                    btnGuardar.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnGuardar.setOnClickListener {
            val hilo = inputHilo.text.toString().uppercase().trim()
            val cantidad = inputCantidad.text.toString().toIntOrNull()

            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = StockSingleton.listaStock.find { it.hiloId == hilo }
            if (item != null) {
                item.madejas += cantidad
                adaptadorStock.actualizarHilo(item)
                dialog.dismiss()
            }
        }
        btnVolver.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun dialogEliminarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val idHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloMadejasEliminarStk)
        val cantidadEliminar = dialog.findViewById<EditText>(R.id.edTxt_edTxt_introducirNumMadejasEliminarStk)
        val txtMadejasActuales = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnEliminar = dialog.findViewById<Button>(R.id.btn_eliminarMadejaConfirmarStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)

        var hiloEncontrado: HiloStock? = null

        idHilo.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hiloId = s.toString().uppercase().trim()
                hiloEncontrado = StockSingleton.listaStock.find { it.hiloId == hiloId }

                if (hiloEncontrado != null) {
                    txtMadejasActuales.text = "Madejas actuales: ${hiloEncontrado!!.madejas}"
                    btnEliminar.isEnabled = true
                } else {
                    txtMadejasActuales.text = "Madejas actuales: -"
                    btnEliminar.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnEliminar.setOnClickListener {
            val cantidadStr = cantidadEliminar.text.toString().trim()
            val cantidad = cantidadStr.toIntOrNull()

            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (hiloEncontrado != null) {
                hiloEncontrado!!.madejas = maxOf(0, hiloEncontrado!!.madejas - cantidad)
                adaptadorStock.actualizarHilo(hiloEncontrado!!)
                dialog.dismiss()
            }
        }

        btnVolver.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun dialogEliminarHilo(posicion: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloStk)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarHilo)
        val hiloABorrar = dialog.findViewById<TextView>(R.id.txtVw_confirmarEliminarHiloStk)

        val hiloEliminado = StockSingleton.listaStock[posicion].hiloId
        val textoOriginal = getString(R.string.confirmarEliminarHiloStk)
        val textoConHilo = textoOriginal.replace("%s", hiloEliminado)
        val spannable = SpannableString(textoConHilo)
        val start = textoConHilo.indexOf(hiloEliminado)
        val end = start + hiloEliminado.length

        if (start != -1) {
            spannable.setSpan(ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        hiloABorrar.text = spannable

        btnVolver.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            StockSingleton.listaStock.removeAt(posicion)
            adaptadorStock.actualizarLista(StockSingleton.listaStock)
            Toast.makeText(this, "Hilo '$hiloEliminado' eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }
}
