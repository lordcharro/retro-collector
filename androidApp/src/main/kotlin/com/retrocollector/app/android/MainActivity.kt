package com.retrocollector.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.retrocollector.app.App
import com.retrocollector.app.RetroCollectorApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (RetroCollectorApplication.appContext == null) {
            RetroCollectorApplication.appContext = applicationContext
        }

        setContent {
            App()
        }
    }
}
