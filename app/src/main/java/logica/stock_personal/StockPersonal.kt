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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloStockDao
import persistencia.entidades.HiloStockEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ValidarFormatoHilos
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.ordenarHilos

/**
 * Actividad que gestiona el inventario personal de hilos del usuario.
 * Permite visualizar, agregar, eliminar hilos y modificar el número de madejas disponibles.
 * También incluye un buscador por código de hilo.
 *
 * @author Olga y Sandra Macías Aragón
 */
class StockPersonal : BaseActivity() {

    private lateinit var tablaStock: RecyclerView
    private lateinit var adaptadorStock: AdaptadorStock
    private lateinit var dao: HiloStockDao
    private var userId: Int = -1

    // Trabajamos siempre sobre esta lista local:
    private val listaStock = mutableListOf<HiloStock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_aa_principal)
        funcionToolbar(this)

        // Inicializa DAO y sesión
        dao = ThreadlyDatabase.getDatabase(applicationContext).hiloStockDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()

        // RecyclerView y Adaptador
        tablaStock = findViewById(R.id.tabla_stock)
        adaptadorStock = AdaptadorStock(listaStock, ::dialogEliminarHilo)
        tablaStock.layoutManager = LinearLayoutManager(this)
        tablaStock.adapter = adaptadorStock

        // Primera carga: si no hay stock para este usuario, cargo el XML
        lifecycleScope.launch(Dispatchers.IO) {
            refrescarUI()
        }

        // Botones
        findViewById<Button>(R.id.btn_agregarHiloStk).setOnClickListener { dialogAgregarHilo() }
        findViewById<Button>(R.id.btn_agregarMadejaStk).setOnClickListener { dialogAgregarMadeja() }
        findViewById<Button>(R.id.btn_eliminarMadejaStk).setOnClickListener { dialogEliminarMadeja() }

        buscadorHilo()
    }

    /** Refresca la RV leyendo de Room y ordenando. */
    private fun refrescarUI() {
        lifecycleScope.launch(Dispatchers.IO) {
            val entidades = dao.obtenerStockPorUsuario(userId)
            val dominio = entidades.map { HiloStock(it.hiloId, it.madejas) }
            val ordenada = ordenarHilos(dominio) { it.hiloId }

            withContext(Dispatchers.Main) {
                listaStock.clear()
                listaStock.addAll(ordenada)
                adaptadorStock.actualizarLista(listaStock)
            }
        }
    }

    /** Buscador: resalta o muestra mensaje si no hay. */
    private fun buscadorHilo() {
        val edt = findViewById<EditText>(R.id.edTxt_buscadorHilo)
        val btn = findViewById<ImageButton>(R.id.imgBtn_lupaStock)
        val txtNo = findViewById<TextView>(R.id.txtVw_sinResultados)
        txtNo.visibility = View.GONE

        btn.setOnClickListener {
            val code = edt.text.toString().trim().uppercase()
            val found = listaStock.find { it.hiloId == code }
            if (found != null) {
                adaptadorStock.resaltarHilo(found.hiloId)
                adaptadorStock.actualizarLista(listaStock)
                tablaStock.scrollToPosition(listaStock.indexOf(found))
                tablaStock.visibility = View.VISIBLE
                txtNo.visibility = View.GONE
            } else {
                tablaStock.visibility = View.GONE
                txtNo.visibility = View.VISIBLE
            }
        }

        edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorStock.resaltarHilo(null)
                    adaptadorStock.actualizarLista(listaStock)
                    tablaStock.visibility = View.VISIBLE
                    txtNo.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /** Diálogo para añadir un hilo nuevo. */
    private fun dialogAgregarHilo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpHilo = dialog.findViewById<EditText>(R.id.edTxt_introducirHilo_dialog_addHilo)
        val inpMadejas = dialog.findViewById<EditText>(R.id.edTxt_introducirMadeja_dialog_addHilo)
        dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_agregarHilo)
            .setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btn_botonAgregarHiloStk)
            .setOnClickListener {
                val hilo = inpHilo.text.toString().trim().uppercase()
                val madejas = inpMadejas.text.toString().toIntOrNull() ?: -1
                if (hilo.isEmpty() || madejas < 0) {
                    Toast.makeText(this, "Campos inválidos", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (!ValidarFormatoHilos.formatoValidoHilo(hilo)) {
                    Toast.makeText(this, "Formato inválido", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    val existe = dao.obtenerPorHiloUsuario(hilo, userId)
                    if (existe != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@StockPersonal,
                                "Ya existe ese hilo",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        dao.insertarStock(
                            HiloStockEntity(
                                usuarioId = userId,
                                hiloId = hilo,
                                madejas = madejas
                            )
                        )

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@StockPersonal, "Hilo añadido", Toast.LENGTH_SHORT)
                                .show()
                            refrescarUI()
                            dialog.dismiss()
                        }
                    }
                }
            }

        dialog.show()
    }

    /** Diálogo para sumar madejas a un hilo existente. */
    private fun dialogAgregarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_agregar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpH = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk_hilo)
        val inpC = dialog.findViewById<EditText>(R.id.edTxt_agregarMadejasStk)
        val lbl = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnG = dialog.findViewById<Button>(R.id.btn_guardarMadejaStk)
        dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)
            .setOnClickListener { dialog.dismiss() }

        inpH.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val code = s.toString().trim().uppercase()
                lifecycleScope.launch(Dispatchers.IO) {
                    val ent = dao.obtenerPorHiloUsuario(code, userId)
                    withContext(Dispatchers.Main) {
                        if (ent != null) {
                            lbl.text = "Madejas actuales: ${ent.madejas}"
                            inpC.isEnabled = true
                            btnG.isEnabled = true
                        } else {
                            lbl.text = "Madejas actuales: -"
                            inpC.isEnabled = false
                            btnG.isEnabled = false
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnG.setOnClickListener {
            val code = inpH.text.toString().trim().uppercase()
            val add = inpC.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch(Dispatchers.IO) {
                val ent = dao.obtenerPorHiloUsuario(code, userId)
                if (ent != null && add > 0) {
                    val upd = ent.copy(madejas = ent.madejas + add)
                    dao.actualizarStock(upd)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@StockPersonal, "Madejas sumadas", Toast.LENGTH_SHORT)
                            .show()
                        refrescarUI()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    /** Diálogo para restar madejas de un hilo existente. */
    private fun dialogEliminarMadeja() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_madeja)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpH = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloMadejasEliminarStk)
        val inpC = dialog.findViewById<EditText>(R.id.edTxt_edTxt_introducirNumMadejasEliminarStk)
        val lbl = dialog.findViewById<TextView>(R.id.txtVw_madejasActualesStk)
        val btnE = dialog.findViewById<Button>(R.id.btn_eliminarMadejaConfirmarStk)
        dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarMadeja)
            .setOnClickListener { dialog.dismiss() }

        var current: HiloStockEntity? = null

        inpH.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val code = s.toString().trim().uppercase()
                lifecycleScope.launch(Dispatchers.IO) {
                    current = dao.obtenerPorHiloUsuario(code, userId)
                    withContext(Dispatchers.Main) {
                        if (current != null) {
                            lbl.text = "Madejas actuales: ${current!!.madejas}"
                            btnE.isEnabled = true
                        } else {
                            lbl.text = "Madejas actuales: -"
                            btnE.isEnabled = false
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnE.setOnClickListener {
            val del = inpC.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch(Dispatchers.IO) {
                current?.let {
                    val nueva = maxOf(0, it.madejas - del)
                    val upd = it.copy(madejas = nueva)
                    dao.actualizarStock(upd)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@StockPersonal,
                            "Madejas actualizadas",
                            Toast.LENGTH_SHORT
                        ).show()
                        refrescarUI()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    /** Diálogo de confirmación para eliminar un hilo completamente. */
    private fun dialogEliminarHilo(pos: Int) {
        val hilo = listaStock[pos]

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.stock_dialog_eliminar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnE = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloStk)
        val btnV = dialog.findViewById<Button>(R.id.btn_volver_stock_dialog_eliminarHilo)
        val lbl = dialog.findViewById<TextView>(R.id.txtVw_confirmarEliminarHiloStk)

        val plantilla = getString(R.string.confirmarEliminarHiloStk)
        val texto = plantilla.replace("%s", hilo.hiloId)
        val span = SpannableString(texto).apply {
            val start = texto.indexOf(hilo.hiloId)
            setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                start + hilo.hiloId.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        lbl.text = span

        btnV.setOnClickListener { dialog.dismiss() }
        btnE.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.eliminarPorUsuarioYHilo(userId, hilo.hiloId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@StockPersonal,
                        "Hilo '${hilo.hiloId}' eliminado",
                        Toast.LENGTH_SHORT
                    ).show()
                    refrescarUI()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
