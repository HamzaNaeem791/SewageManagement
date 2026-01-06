package com.example.sewagemanagement.di

import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.data.repository.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

/**
 * Container for Dependency Injection.
 * This class manages the creation and lifecycle of application-wide dependencies.
 */
class AppContainer {

    // Firebase Instances (Lazy initialization)
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance() }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore, functions)
    }

    val complaintRepository: ComplaintRepository by lazy {
        ComplaintRepository(firestore)
    }
}
