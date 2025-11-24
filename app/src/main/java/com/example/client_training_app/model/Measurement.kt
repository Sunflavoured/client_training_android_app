package com.example.client_training_app.model

// Tato třída slouží pro aplikační logiku (fragmenty, repository, business logika)
data class Measurement(
    val measurementId: Int = 0,
    val clientId: String, // Odkaz na klienta
    val date: Long = System.currentTimeMillis(),

    // --- MĚŘENÍ TĚLESNÉ VÁHY A OBVODY ---
    val weight: Double?,
    val bustCm: Double?,
    val chestCm: Double?,
    val waistCm: Double?,
    val abdomenCm: Double?,
    val hipsCm: Double?,
    val thighCm: Double?,
    val armCm: Double?
)