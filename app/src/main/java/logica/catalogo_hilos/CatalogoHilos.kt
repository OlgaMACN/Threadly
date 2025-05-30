package logica.catalogo_hilos

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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
import persistencia.daos.HiloCatalogoDao
import persistencia.entidades.HiloCatalogoEntity
import utiles.BaseActivity
import utiles.SesionUsuario
import utiles.funciones.ValidarFormatoHilos
import utiles.funciones.ajustarDialog
import utiles.funciones.funcionToolbar
import utiles.funciones.leerXML
import utiles.funciones.ordenarHilos

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

    private fun refrescarUI() {
        lifecycleScope.launch(Dispatchers.IO) {
            entidades = dao.obtenerHilosPorUsuario(userId)

            listaCatalogo = ordenarHilos(entidades.map { e ->
                HiloCatalogo(e.numHilo, e.nombreHilo, e.color)
            }) { it.numHilo }.toMutableList()

            withContext(Dispatchers.Main) {
                adaptadorCatalogo.actualizarLista(listaCatalogo)
            }
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
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpNum = dialog.findViewById<EditText>(R.id.edTxt_agregarNumHilo)
        val inpNombre = dialog.findViewById<EditText>(R.id.editTxt_agregarNombreHilo)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverAgregarHilo)
        val btnSave = dialog.findViewById<Button>(R.id.btn_guardarAgregarHilo)

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val num = inpNum.text.toString().uppercase().trim()
            val nom = inpNombre.text.toString().trim() /* para respetar el formato que ponga el usuario */
            if (num.isEmpty() || nom.isEmpty()) {
                Toast.makeText(this, "Ningún campo puede estar vacío", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!ValidarFormatoHilos.formatoValidoHilo(num)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                // BONUS: validamos manualmente si ya existe
                val existente = dao.obtenerHiloPorNumYUsuario(num, userId)

                withContext(Dispatchers.Main) {
                    if (existente != null) {
                        Toast.makeText(
                            this@CatalogoHilos,
                            "Ya existe un hilo con ese número",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // No existe, lo insertamos
                        val ent = HiloCatalogoEntity(
                            userId = userId,
                            numHilo = num,
                            nombreHilo = nom
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            dao.insertarHilo(ent)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@CatalogoHilos,
                                    "Hilo añadido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refrescarUI()
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private fun dialogModificarHiloCatalogo() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpNum = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHilo)
        val chkNum = dialog.findViewById<CheckBox>(R.id.ckBx_numHiloModificar)
        val chkNom = dialog.findViewById<CheckBox>(R.id.ckB_nombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHilo)
        val btnNext = dialog.findViewById<Button>(R.id.btn_botonSiguienteModificar)

        btnVolver.setOnClickListener { dialog.dismiss() }
        btnNext.setOnClickListener {
            val numBusq = inpNum.text.toString().trim().uppercase()
            if (numBusq.isEmpty()) {
                Toast.makeText(this, "Introduce el número a modificar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!chkNum.isChecked && !chkNom.isChecked) {
                Toast.makeText(this, "Selecciona un campo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val entidad = dao.obtenerHiloPorNumYUsuario(numBusq, userId)
                withContext(Dispatchers.Main) {
                    if (entidad == null) {
                        Toast.makeText(this@CatalogoHilos, "No existe ese hilo", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        dialog.dismiss()
                        dialogModificarHiloFinal(entidad, chkNum.isChecked, chkNom.isChecked)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun dialogModificarHiloFinal(
        entidadVieja: HiloCatalogoEntity,
        modNum: Boolean,
        modNom: Boolean
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_modificar_final)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val inpNum = dialog.findViewById<EditText>(R.id.edTxt_introducirNumHiloModificar)
        val inpNombre = dialog.findViewById<EditText>(R.id.editTxt_introducirNombreHiloModificar)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_volverModificarHiloFinal)
        val btnSave = dialog.findViewById<Button>(R.id.btn_guardarModificarHilo)

        if (modNum) inpNum.setText(entidadVieja.numHilo) else inpNum.visibility = View.GONE
        if (modNom) inpNombre.setText(entidadVieja.nombreHilo) else inpNombre.visibility = View.GONE

        btnVolver.setOnClickListener {
            dialog.dismiss()
            dialogModificarHiloCatalogo()
        }

        btnSave.setOnClickListener {
            val nuevoNum =
                if (modNum) inpNum.text.toString().uppercase().trim() else entidadVieja.numHilo
            val nuevoNom = if (modNom) inpNombre.text.toString()
                .trim() else entidadVieja.nombreHilo /* para respetar el formato que ponga el usuario */

            if ((modNum && nuevoNum.isEmpty()) || (modNom && nuevoNom.isEmpty())) {
                Toast.makeText(this, "Ningún campo vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (modNum && !ValidarFormatoHilos.formatoValidoHilo(nuevoNum)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                // BONUS: validación si se modifica el número
                if (modNum && nuevoNum != entidadVieja.numHilo) {
                    val existente = dao.obtenerHiloPorNumYUsuario(nuevoNum, userId)
                    if (existente != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CatalogoHilos,
                                "Ya existe un hilo con ese número",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch // abortar
                    }
                }

                val entidadNueva = entidadVieja.copy(
                    numHilo = nuevoNum,
                    nombreHilo = nuevoNom
                )
                dao.actualizarHilo(entidadNueva)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CatalogoHilos, "Hilo modificado", Toast.LENGTH_SHORT).show()
                    refrescarUI()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun dialogEliminarHiloCatalogo(hilo: HiloCatalogo) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.catalogo_dialog_eliminar_hilo)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        ajustarDialog(dialog)
        dialog.setCancelable(false)

        val btnEliminar = dialog.findViewById<Button>(R.id.btn_botonEliminarHiloCat)
        val btnVolver = dialog.findViewById<Button>(R.id.btn_botonVolverEliminarCat)
        val txtMsg = dialog.findViewById<TextView>(R.id.txtVw_mensajeEliminarHiloCat)

        val plantilla = getString(R.string.confirmarEliminarHiloCat)
        val texto = plantilla.replace("%s", hilo.numHilo)
        val span = SpannableString(texto)
        val start = texto.indexOf(hilo.numHilo)
        val end = start + hilo.numHilo.length
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
                dao.eliminarPorNumYUsuario(hilo.numHilo, userId) // O usa el ID si lo tienes
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CatalogoHilos,
                        "Hilo ${hilo.numHilo} eliminado",
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
