package com.example.sewagemanagement.data.repository

import com.example.sewagemanagement.data.model.User
import com.example.sewagemanagement.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions
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

            // Citizen signup only. Admin is seeded; workers are created by admin.
            val role = "citizen"

            val user = User(
                userId = userId, 
                name = name, 
                email = email,
                role = role
            )
            db.collection("users").document(userId).set(user).await()
            
            Resource.Success("Registration Successful")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration Failed")
        }
    }

    suspend fun createWorkerAccount(
        name: String,
        email: String,
        password: String,
        phoneNumber: String = "",
        address: String = ""
    ): Resource<String> {
        return try {
            val data = hashMapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "phoneNumber" to phoneNumber,
                "address" to address
            )

            val result = functions
                .getHttpsCallable("createWorkerUser")
                .call(data)
                .await()

            val uid = (result.data as? Map<*, *>)?.get("uid") as? String
            Resource.Success(uid ?: "Worker created")
        } catch (e: FirebaseFunctionsException) {
            Resource.Error(e.message ?: "Failed to create worker")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create worker")
        }
    }
    
    fun getCurrentUser() = auth.currentUser
    
    fun logout() {
        auth.signOut()
    }

    suspend fun getUser(userId: String): Resource<User> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Resource.Success(user)
                } else {
                    Resource.Error("User data parsing failed")
                }
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getUsersByRole(role: String): Resource<List<User>> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", role)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch users")
        }
    }

    suspend fun updateUser(user: User): Resource<String> {
        return try {
            db.collection("users").document(user.userId).set(user).await()
            Resource.Success("Profile updated successfully")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}
