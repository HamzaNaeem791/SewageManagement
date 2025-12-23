package com.example.sewagemanagement

import android.app.Application
import com.example.sewagemanagement.di.AppContainer

class SewageApplication : Application() {

    // Instance of AppContainer that will be used by other classes
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
