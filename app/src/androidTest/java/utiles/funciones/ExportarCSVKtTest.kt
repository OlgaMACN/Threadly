package utiles.funciones

import android.os.Build
import org.junit.Assert.*
import org.junit.Test


class ExportarCSVKtTest {

    @Test
    fun testExportarPedidoCSV_OK() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        //Mock Context y content
        //val context = Mockito.mock
    }
}