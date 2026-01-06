package com.example.sewagemanagement.di

import android.content.Context
import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.data.repository.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.FirebaseApp

/**
 * Container for Dependency Injection.
 * This class manages the creation and lifecycle of application-wide dependencies.
 */
class AppContainer(private val appContext: Context) {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance("us-central1") }

    // Secondary FirebaseAuth instance (separate FirebaseApp) so admin can create worker accounts
    // without affecting the primary session.
    private val workerCreatorFirebaseApp: FirebaseApp by lazy {
        val appName = "workerCreator"
        FirebaseApp.getApps(appContext).firstOrNull { it.name == appName }
            ?: FirebaseApp.initializeApp(appContext, FirebaseApp.getInstance().options, appName)
            ?: throw IllegalStateException("Failed to initialize secondary FirebaseApp")
    }

    private val workerCreatorAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance(workerCreatorFirebaseApp)
    }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore, functions, workerCreatorAuth)
    }

    val complaintRepository: ComplaintRepository by lazy {
        ComplaintRepository(firestore)
    }
}
