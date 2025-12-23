package com.example.sewagemanagement.data.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Complaint(
    val complaintId: String = "",
    val userId: String = "",
    val issueType: String = "",
    val description: String = "",
    val location: GeoPoint? = null,
    val status: String = "Pending", // "Pending", "In Progress", "Resolved"
    val assignedTo: String = "", // Worker UserId
    val imageUrl: String = "",
    val timestamp: Date = Date()
)
