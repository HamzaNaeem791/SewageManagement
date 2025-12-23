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
                .get()
                .await()
            val complaints = snapshot.toObjects(Complaint::class.java)
                .sortedByDescending { it.timestamp }
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
            val snapshot = db.collection("complaints")
                .whereEqualTo("timestamp", java.util.Date(complaintId.toLong()))
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

    suspend fun getComplaintsForWorker(workerId: String): Resource<List<Complaint>> {
        return try {
            val snapshot = db.collection("complaints")
                .whereEqualTo("assignedTo", workerId)
                .get()
                .await()
            val complaints = snapshot.toObjects(Complaint::class.java)
                .sortedByDescending { it.timestamp }
            Resource.Success(complaints)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch jobs")
        }
    }

    suspend fun assignComplaint(complaintId: String, workerId: String): Resource<String> {
        return try {
            val snapshot = db.collection("complaints")
                .whereEqualTo("timestamp", java.util.Date(complaintId.toLong()))
                .get().await()
            
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                db.collection("complaints").document(doc.id).update(
                    "assignedTo", workerId,
                    "status", "In Progress" // Auto-set to In Progress when assigned
                ).await()
                Resource.Success("Job assigned successfully")
            } else {
                Resource.Error("Complaint not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Assignment failed")
        }
    }
}
