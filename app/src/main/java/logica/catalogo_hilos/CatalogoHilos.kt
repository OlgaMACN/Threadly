package logica.catalogo_hilos

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.threadly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.HiloCatalogoDao

import persistencia.entidades.HiloCatalogoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ValidarFormatoHilos
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.leerXML
/*** @author Olga y Sandra Macías Aragón*/
class CatalogoHilos : BaseActivity() {


    private var entidades: List<HiloCatalogoEntity> = emptyList()

    private var listaCatalogo = mutableListOf<HiloCatalogo>()

    private lateinit var tablaCatalogo: RecyclerView
    private lateinit var adaptadorCatalogo: AdaptadorCatalogo

    private lateinit var dao: HiloCatalogoDao
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogo_aa_hilos)
        funcionToolbar(this)

        dao = ThreadlyDatabase
            .getDatabase(applicationContext)
            .hiloCatalogoDao()
        userId = SesionUsuario.obtenerSesion(this)
        if (userId < 0) finish()


        tablaCatalogo = findViewById(R.id.tabla_catalogo)
        adaptadorCatalogo = AdaptadorCatalogo(listaCatalogo, ::dialogEliminarHiloCatalogo)
        tablaCatalogo.layoutManager = LinearLayoutManager(this)
        tablaCatalogo.adapter = adaptadorCatalogo

        /* primera carga para el xml */
        lifecycleScope.launch(Dispatchers.IO) {
            val existentes = dao.obtenerHilosPorUsuario(userId)
            if (existentes.isEmpty()) {
                val xmlList = leerXML(this@CatalogoHilos, R.raw.catalogo_hilos)
                val entidadesXml = xmlList.map { hc ->
                    HiloCatalogoEntity(
                        userId = userId,
                        numHilo = hc.numHilo,
                        nombreHilo = hc.nombreHilo,
                        color = hc.color
                    )
                }
                dao.insertarHilos(entidadesXml)
            }
            refrescarUI()
        }


        findViewById<Button>(R.id.btn_agregarHiloConsulta)
            .setOnClickListener { dialogAgregarHiloCatalogo() }
        findViewById<Button>(R.id.btn_modificarHiloConsulta)
            .setOnClickListener { dialogModificarHiloCatalogo() }

        buscadorHilo()
    }

    private suspend fun refrescarUI() {
        entidades = dao.obtenerHilosPorUsuario(userId)

        listaCatalogo = entidades.map { e ->
            HiloCatalogo(e.numHilo, e.nombreHilo, e.color)
        }.toMutableList()

        withContext(Dispatchers.Main) {
            adaptadorCatalogo.actualizarLista(listaCatalogo)
        }
    }

    private fun buscadorHilo() {
        val editText = findViewById<EditText>(R.id.txtVw_buscarHiloConsulta)
        val btnLupa = findViewById<ImageButton>(R.id.imgBtn_lupaCatalogo)
        val txtNoRes = findViewById<TextView>(R.id.txtVw_sinResultadosCatalogo)
        txtNoRes.visibility = View.GONE

        btnLupa.setOnClickListener {
            val busq = editText.text.toString().trim().uppercase()
            val found = listaCatalogo.find {
                it.numHilo == busq ||
                        it.nombreHilo.contains(busq, ignoreCase = true)
            }
            if (found != null) {
                val idx = listaCatalogo.indexOf(found)
                adaptadorCatalogo.resaltarHilo(found.numHilo)
                adaptadorCatalogo.actualizarLista(listaCatalogo)
                tablaCatalogo.scrollToPosition(idx)
                tablaCatalogo.visibility = View.VISIBLE
                txtNoRes.visibility = View.GONE
            } else {
                tablaCatalogo.visibility = View.GONE
                txtNoRes.visibility = View.VISIBLE
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adaptadorCatalogo.resaltarHilo(null)
                    adaptadorCatalogo.actualizarLista(listaCatalogo)
                    tablaCatalogo.visibility = View.VISIBLE
                    txtNoRes.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun dialogAgregarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_agregar_hilo)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpNum = dialog.findViewById<EditText>(R.id.edTxt_agregarNumHilo)
        val inpNombre = dialog.findViewById<EditText>(R.id.editTxt_agregarNombreHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverAgregarHilo)
        val btnSave = dialog.findViewById<Button>(R.id.btn_guardarAgregarHilo)

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val num = inpNum.text.toString().uppercase().trim()
            val nom = inpNombre.text.toString().uppercase().trim()
            if (num.isEmpty() || nom.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!ValidarFormatoHilos.formatoValidoHilo(num)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val ent = HiloCatalogoEntity(
                    userId = userId,
                    numHilo = num,
                    nombreHilo = nom
                )
                val res = dao.insertarHilo(ent)
                withContext(Dispatchers.Main) {
                    if (res == -1L) {
                        Toast.makeText(this@CatalogoHilos, "El número ya existe", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(this@CatalogoHilos, "Hilo añadido", Toast.LENGTH_SHORT)
                            .show()
                        lifecycleScope.launch { refrescarUI() }
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun dialogModificarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpNum = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHilo)
        val chkNum = dialog.findViewById<CheckBox>(R.id.ckBx_numHiloModificar)
        val chkNom = dialog.findViewById<CheckBox>(R.id.ckB_nombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHilo)
        val btnNext = dialog.findViewById<Button>(R.id.btn_botonSiguienteModificar)

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnNext.setOnClickListener {
            val numBusq = inpNum.text.toString().trim()
            if (numBusq.isEmpty()) {
                Toast.makeText(this, "Introduce el número a modificar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val pos = listaCatalogo.indexOfFirst {
                it.numHilo.equals(numBusq, ignoreCase = true)
            }
            if (pos == -1) {
                Toast.makeText(this, "No existe ese hilo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!chkNum.isChecked && !chkNom.isChecked) {
                Toast.makeText(this, "Selecciona un campo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            dialogModificarHiloFinal(pos, chkNum.isChecked, chkNom.isChecked)
        }

        dialog.show()
    }

    private fun dialogModificarHiloFinal(
        pos: Int,
        modNum: Boolean,
        modNom: Boolean
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar_final)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpNum = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloModificar)
        val inpNombre = dialog.findViewById<EditText>(R.id.editTxt_introducirNombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHiloFinal)
        val btnSave = dialog.findViewById<Button>(R.id.btn_guardarModificarHilo)

        // cargo los valores actuales
        val entidadVieja = entidades[pos]
        if (modNum) inpNum.setText(entidadVieja.numHilo) else inpNum.visibility = View.GONE
        if (modNom) inpNombre.setText(entidadVieja.nombreHilo) else inpNombre.visibility = View.GONE

        btnVolver.setOnClickListener {
            dialog.dismiss()
            dialogModificarHiloCatalogo()
        }
        btnSave.setOnClickListener {
            val nuevoNum =
                if (modNum) inpNum.text.toString().uppercase().trim() else entidadVieja.numHilo
            val nuevoNom = if (modNom) inpNombre.text.toString().uppercase()
                .trim() else entidadVieja.nombreHilo

            if ((modNum && nuevoNum.isEmpty()) || (modNom && nuevoNom.isEmpty())) {
                Toast.makeText(this, "Ningún campo vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (modNum && !ValidarFormatoHilos.formatoValidoHilo(nuevoNum)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val entidadNueva = entidadVieja.copy(
                    numHilo = nuevoNum,
                    nombreHilo = nuevoNom
                )
                dao.actualizarHilo(entidadNueva)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CatalogoHilos, "Hilo modificado", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch { refrescarUI() }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun dialogEliminarHiloCatalogo(pos: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_eliminar_hilo)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloCat)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_botonVolverEliminarCat)
        val txtMsg = dialog.findViewById<TextView>(R.id.txtVw_mensajeEliminarHiloCat)

        val entidad = entidades[pos]
        val plantilla = getString(R.string.confirmarEliminarHiloCat)
        val texto = plantilla.replace("%s", entidad.numHilo)
        val span = SpannableString(texto)
        val start = texto.indexOf(entidad.numHilo)
        val end = start + entidad.numHilo.length
        span.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        txtMsg.text = span

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnEliminar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.eliminarHilo(entidad)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CatalogoHilos,
                        "Hilo ${entidad.numHilo} eliminado",
                        Toast.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch { refrescarUI() }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
