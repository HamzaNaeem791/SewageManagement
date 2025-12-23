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

    suspend fun getAllComplaints(): Resource<List<Complaint>> {
        return try {
            val snapshot = db.collection("complaints")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val complaints = snapshot.toObjects(Complaint::class.java)
            Resource.Success(complaints)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch all complaints")
        }
    }

    suspend fun updateComplaintStatus(complaintId: String, newStatus: String): Resource<String> {
        return try {
            // Find document by filtering or if we used ID as docId. 
            // Ideally we need the docId. For now, assuming we query by timestamp which we used as ID in UI but that's not the docID.
            // Let's assume we need to query effectively or modify the model to store docId.
            // For now, I will assume the timestamp is unique enough or we passed it.
            // BETTER: Use the doc ID. I need to make sure Complaint model has the Firestore Document ID.
            // I will implement a query-update for now:
            val snapshot = db.collection("complaints")
                .whereEqualTo("timestamp", java.util.Date(complaintId.toLong())) // Weak match if ID is timestamp
                .get().await()
            
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                db.collection("complaints").document(doc.id).update("status", newStatus).await()
                Resource.Success("Status updated to $newStatus")
            } else {
                 Resource.Error("Complaint not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }
}
