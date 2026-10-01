package com.retrocollector.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RetroCollectorApplication.appContext == null) {
            RetroCollectorApplication.appContext = applicationContext
        }

        setContent {
            App()
        }
    }
}
