package com.example.sewagemanagement

import android.app.Application
import android.util.Log
import com.example.sewagemanagement.di.AppContainer
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp

class SewageApplication : Application() {

    // Instance of AppContainer that will be used by other classes
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        try {
            val options = FirebaseApp.getInstance().options
            Log.d(
                "WorkerCreate",
                "Firebase options: projectId=${options.projectId}, applicationId=${options.applicationId}"
            )
        } catch (e: Exception) {
            Log.w("WorkerCreate", "Failed to read FirebaseApp options", e)
        }

        MobileAds.initialize(this) {
            Log.d("AdMobBanner", "MobileAds initialized")
        }
    }
}
