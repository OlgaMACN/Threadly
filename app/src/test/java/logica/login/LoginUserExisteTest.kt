package logica.login

import android.content.Intent
import android.os.Build
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.threadly.R
import io.mockk.coEvery // Importar coEvery para funciones suspend
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import logica.pantalla_inicio.PantallaPrincipal
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import persistencia.bbdd.ThreadlyDatabase
import persistencia.daos.UsuarioDAO
import persistencia.entidades.Usuario // Importar Usuario de persistencia.entidades
import utiles.SesionUsuario

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LoginUserExisteTest {

    private lateinit var scenario: ActivityScenario<LoginUserExiste>
    private lateinit var mockUsuarioDAO: UsuarioDAO

    @Before
    fun setUp() {
        // Mock de la base de datos y el DAO
        mockUsuarioDAO = mockk<UsuarioDAO>()
        mockkStatic(ThreadlyDatabase::class) // Mockear métodos estáticos
         coEvery { ThreadlyDatabase.getDatabase(any()) } returns mockk {
            coEvery { usuarioDAO() } returns mockUsuarioDAO
        }

        // Mockear métodos estáticos de SesionUsuario
        mockkStatic(SesionUsuario::class)
       coEvery { SesionUsuario.guardarSesion(any(), any()) } returns Unit

        // Inicia la actividad bajo prueba
        scenario = ActivityScenario.launch(LoginUserExiste::class.java)
    }

    @After
    fun tearDown() {
        // Cierra el escenario de la actividad
        scenario.close()
    }

    /**
     * Test para el método inicializarVistas.
     * Verifica que las vistas se inicializan correctamente y tienen los IDs esperados.
     */
    @Test
    fun `inicializarVistas should initialize all view components`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val botonOjoImageView: ImageView = activity.findViewById(R.id.imgVw_eye_closed)

            assertEquals(R.id.edTxt_ingresarNombreUser, usuarioEditText.id)
            assertEquals(R.id.edTxt_ingresarConstrasenaUser, contrasenaEditText.id)
            assertEquals(R.id.imgVw_eye_closed, botonOjoImageView.id)
        }
    }

    /**
     * Test para el método configurarBotonOjo.
     * Verifica que el botón de ojo alterna la visibilidad de la contraseña y el ícono.
     */
    @Test
    fun `configurarBotonOjo should toggle password visibility and eye icon`() {
        scenario.onActivity { activity ->
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val eyeIcon: ImageView = activity.findViewById(R.id.imgVw_eye_closed)

            // Estado inicial: contraseña oculta, icono de ojo cerrado
            assertEquals(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, contrasenaEditText.inputType)
            assertEquals(R.drawable.img_eye_closed, Shadows.shadowOf(eyeIcon.drawable).createdFromResId)

            // Simular clic para mostrar contraseña
            eyeIcon.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks() // Procesar tareas de UI pendientes
            assertEquals(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, contrasenaEditText.inputType)
            assertEquals(R.drawable.img_eye_open, Shadows.shadowOf(eyeIcon.drawable).createdFromResId)

            // Simular otro clic para ocultar contraseña
            eyeIcon.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks() // Procesar tareas de UI pendientes
            assertEquals(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, contrasenaEditText.inputType)
            assertEquals(R.drawable.img_eye_closed, Shadows.shadowOf(eyeIcon.drawable).createdFromResId)
        }
    }

    /**
     * Test para el método configurarBotonEntrar.
     * Verifica que al hacer clic en el botón de "Entrar", se llama a intentarIniciarSesion.
     * Podemos inferir su ejecución observando el comportamiento que debería desencadenar
     * (por ejemplo, un Toast si los campos están vacíos).
     */
    @Test
    fun `configurarBotonEntrar should trigger login attempt on click`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val loginButton: Button = activity.findViewById(R.id.btn_ingresarThreadly)

            // Dejar campos vacíos para provocar un Toast (comportamiento conocido de intentarIniciarSesion)
            usuarioEditText.setText("")
            contrasenaEditText.setText("")

            loginButton.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks() // Procesar Toast
            assertEquals("Por favor, rellena todos los campos", ShadowToast.getTextOfLatestToast())
        }
    }

    /**
     * Test para el método configurarCrearCuenta.
     * Verifica que al hacer clic en el texto "Crear Cuenta", se inicia la actividad LoginUserNoExiste.
     */
    @Test
    fun `configurarCrearCuenta should navigate to LoginUserNoExiste`() {
        scenario.onActivity { activity ->
            val crearCuentaTextView: TextView = activity.findViewById(R.id.txtVw_crearCuenta)

            crearCuentaTextView.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks() // Procesar la navegación

            val expectedIntent = Intent(activity, LoginUserNoExiste::class.java)
            val actualIntent = Shadows.shadowOf(activity).nextStartedActivity

            assertEquals(expectedIntent.component, actualIntent.component)
        }
    }

    /**
     * Tests para el método intentarIniciarSesion.
     * Cubre varios escenarios de validación y lógica de negocio.
     */
    @Test
    fun `intentarIniciarSesion with empty fields should show toast and clear inputs`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val loginButton: Button = activity.findViewById(R.id.btn_ingresarThreadly)

            usuarioEditText.setText("")
            contrasenaEditText.setText("")
            loginButton.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

            assertEquals("Por favor, rellena todos los campos", ShadowToast.getTextOfLatestToast())
            assertEquals("", usuarioEditText.text.toString())
            assertEquals("", contrasenaEditText.text.toString())
        }
    }

    @Test
    fun `intentarIniciarSesion with long fields should show toast and clear inputs`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val loginButton: Button = activity.findViewById(R.id.btn_ingresarThreadly)

            usuarioEditText.setText("a".repeat(21)) // Más de 20 caracteres
            contrasenaEditText.setText("b".repeat(21)) // Más de 20 caracteres
            loginButton.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

            assertEquals("Por favor, rellena todos los campos", ShadowToast.getTextOfLatestToast())
            assertEquals("", usuarioEditText.text.toString())
            assertEquals("", contrasenaEditText.text.toString())
        }
    }

    @Test
    fun `intentarIniciarSesion with non-existent user should show toast`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val loginButton: Button = activity.findViewById(R.id.btn_ingresarThreadly)

            val username = "nonExistentUser"
            val password = "somePassword"

            usuarioEditText.setText(username)
            contrasenaEditText.setText(password)

            // Simular que login devuelve null (no coincide usuario/contraseña)
            coEvery { mockUsuarioDAO.login(username, password) } returns null as Usuario? // <-- MODIFICADO
            // Y que getPorNombre también devuelve null (el usuario no existe)
            coEvery { mockUsuarioDAO.getPorNombre(username) } returns null as Usuario? // <-- MODIFICADO

            loginButton.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks() // Procesar UI y coroutines simuladas
            ShadowLooper.idleMainLooper() // Damos un pequeño respiro al Looper

            assertEquals("El usuario introducido no existe", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun `intentarIniciarSesion with incorrect password should show toast`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val loginButton: Button = activity.findViewById(R.id.btn_ingresarThreadly)

            val username = "existingUser"
            val incorrectPassword = "incorrectPassword"
            // ASUMIMOS que Usuario en persistencia.entidades.Usuario no tiene 'profileImageUrl'
            // Ajusta este constructor para que coincida EXACTAMENTE con tu 'Usuario.kt' real.
            val existingUser = Usuario(userId = 1, username = username, password = "correctPassword", profilePic = 2)

            usuarioEditText.setText(username)
            contrasenaEditText.setText(incorrectPassword)

            // Simular que login devuelve null (contraseña incorrecta)
            coEvery { mockUsuarioDAO.login(username, incorrectPassword) } returns null as Usuario? // <-- MODIFICADO
            // Pero que getPorNombre devuelve el usuario (el usuario sí existe)
            coEvery { mockUsuarioDAO.getPorNombre(username) } returns existingUser

            loginButton.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            ShadowLooper.idleMainLooper()

            assertEquals("Contraseña incorrecta", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun `intentarIniciarSesion with successful login should navigate to PantallaPrincipal`() {
        scenario.onActivity { activity ->
            val usuarioEditText: EditText = activity.findViewById(R.id.edTxt_ingresarNombreUser)
            val contrasenaEditText: EditText = activity.findViewById(R.id.edTxt_ingresarConstrasenaUser)
            val loginButton: Button = activity.findViewById(R.id.btn_ingresarThreadly)

            val username = "testUser"
            val password = "testPassword"
            val userId = 100L
            // ASUMIMOS que Usuario en persistencia.entidades.Usuario no tiene 'profileImageUrl'
            // Ajusta este constructor para que coincida EXACTAMENTE con tu 'Usuario.kt' real.
            val mockUser = Usuario(userId = userId.toInt(), username = username, password = password, profilePic = 2)

            usuarioEditText.setText(username)
            contrasenaEditText.setText(password)

            // Simular un login exitoso
            coEvery { mockUsuarioDAO.login(username, password) } returns mockUser

            loginButton.performClick()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            ShadowLooper.idleMainLooper() // Asegura que la coroutine de navegación se complete

            // Verificar que no se muestra ningún Toast
            assertFalse("No se esperaba ningún Toast, pero se mostró uno: ${ShadowToast.getTextOfLatestToast()}", ShadowToast.getTextOfLatestToast() != null)

            // Verificar que se inició la actividad correcta y se pasaron los extras
            val expectedIntent = Intent(activity, PantallaPrincipal::class.java)
            val actualIntent = Shadows.shadowOf(activity).nextStartedActivity

            assertEquals(expectedIntent.component, actualIntent.component)
            assertEquals(username, actualIntent.getStringExtra("nombre_usuario"))
            assertEquals(userId, actualIntent.getLongExtra("usuario_id", -1L))
            assertTrue(activity.isFinishing) // La actividad de login debe finalizar
        }
    }
}