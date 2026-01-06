package com.example.sewagemanagement

import android.app.Application
import android.util.Log
import com.example.sewagemanagement.di.AppContainer
import com.google.android.gms.ads.MobileAds

class SewageApplication : Application() {

    // Instance of AppContainer that will be used by other classes
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()

        MobileAds.initialize(this) {
            Log.d("AdMobBanner", "MobileAds initialized")
        }
    }
}
