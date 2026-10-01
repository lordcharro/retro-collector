package com.retrocollector.app

import android.app.Application
import android.content.Context

class RetroCollectorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        var appContext: Context? = null
            internal set
    }
}
