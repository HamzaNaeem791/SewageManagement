package com.example.sewagemanagement.di

import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.data.repository.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Container for Dependency Injection.
 * This class manages the creation and lifecycle of application-wide dependencies.
 */
class AppContainer {

    // Firebase Instances (Lazy initialization)
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }

    val complaintRepository: ComplaintRepository by lazy {
        ComplaintRepository(firestore)
    }
}
