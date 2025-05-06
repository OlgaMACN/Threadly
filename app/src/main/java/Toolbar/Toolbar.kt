package Toolbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.threadly.R

class Toolbar : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_layout)
    }
}