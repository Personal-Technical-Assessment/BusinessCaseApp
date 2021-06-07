package com.gozem.test.businesscase.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.github.kittinunf.fuel.core.FuelManager
import com.google.firebase.FirebaseApp
import com.gozem.test.businesscase.utils.Constants.API_BASE_URL
import com.gozem.test.businesscase.utils.Prefs
import io.socket.client.Socket

val appContext: Context by lazy { App.appContext!! }
val app: App by lazy { App.app!! }
val prefs: Prefs by lazy { App.prefs!! }

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this@App
        appContext = applicationContext
        prefs = Prefs(applicationContext)
        // Initialize Firebase
        FirebaseApp.initializeApp(applicationContext)
        // Initialize Fuel base URL
        FuelManager.instance.basePath = API_BASE_URL
    }

    companion object {
        var appContext: Context? = null
        var app: App? = null
        @SuppressLint("StaticFieldLeak")
        var prefs: Prefs? = null
        var socket: Socket? = null
    }
}