package com.sensors.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sensors.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mainBinding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(mainBinding!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(mainBinding!!.root) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() and WindowInsetsCompat.Type.tappableElement()
            )
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        mainBinding = null
        super.onDestroy()
    }
}