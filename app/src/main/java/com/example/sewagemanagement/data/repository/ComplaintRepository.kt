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
            val docRef = db.collection("complaints").document()
            val complaintWithId = complaint.copy(complaintId = docRef.id)
            docRef.set(complaintWithId).await()
            Resource.Success(docRef.id)
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
            val complaints = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Complaint::class.java)?.copy(complaintId = doc.id)
                }
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
            val complaints = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Complaint::class.java)?.copy(complaintId = doc.id)
                }
            Resource.Success(complaints)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch all complaints")
        }
    }

    suspend fun updateComplaintStatus(complaintId: String, newStatus: String): Resource<String> {
        return try {
            db.collection("complaints").document(complaintId)
                .update("status", newStatus)
                .await()
            Resource.Success("Status updated to $newStatus")
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
            val complaints = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Complaint::class.java)?.copy(complaintId = doc.id)
                }
                .sortedByDescending { it.timestamp }
            Resource.Success(complaints)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch jobs")
        }
    }

    suspend fun assignComplaint(complaintId: String, workerId: String): Resource<String> {
        return try {
            db.collection("complaints").document(complaintId).update(
                "assignedTo", workerId,
                "status", "In Progress" // Auto-set to In Progress when assigned
            ).await()
            Resource.Success("Job assigned successfully")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Assignment failed")
        }
    }
}
