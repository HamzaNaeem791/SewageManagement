package com.example.sewagemanagement.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val dob: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val role: String = "citizen" // values: "citizen", "worker", "admin"
)
