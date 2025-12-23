package com.example.sewagemanagement.data.repository

import com.example.sewagemanagement.data.model.User
import com.example.sewagemanagement.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {


    suspend fun login(email: String, pass: String): Resource<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success("Login Successful")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login Failed")
        }
    }

    suspend fun register(name: String, email: String, pass: String): Resource<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("User creation failed")
            
            val user = User(userId, name, email)
            db.collection("users").document(userId).set(user).await()
            
            Resource.Success("Registration Successful")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration Failed")
        }
    }
    
    fun getCurrentUser() = auth.currentUser
    
    fun logout() {
        auth.signOut()
    }
}
