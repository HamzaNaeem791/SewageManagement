package com.example.sewagemanagement.data.repository

import com.example.sewagemanagement.data.model.Complaint
import com.example.sewagemanagement.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ComplaintRepository(
    private val db: FirebaseFirestore
) {


    suspend fun submitComplaint(complaint: Complaint): Resource<String> {
        return try {
            db.collection("complaints").add(complaint).await()
            Resource.Success("Complaint Submitted Successfully")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Submission Failed")
        }
    }

    suspend fun getComplaintsForUser(userId: String): Resource<List<Complaint>> {
        return try {
            val snapshot = db.collection("complaints")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val complaints = snapshot.toObjects(Complaint::class.java)
            Resource.Success(complaints)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch complaints")
        }
    }
}
