package com.example.client_training_app.model

data class Client (
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: Long?,
    val email: String?,
    val phone: String?,
    val notes: String?
)