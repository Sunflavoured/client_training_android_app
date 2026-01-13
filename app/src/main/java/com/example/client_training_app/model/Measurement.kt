package com.example.client_training_app.model

import com.example.client_training_app.data.entity.MeasurementEntity

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

// PŘEVOD Z Measurement -> MeasurementEntity
fun Measurement.toEntity() = MeasurementEntity(
    measurementId = measurementId,
    clientId = clientId,
    date = date,
    weight = weight,
    bustCm = bustCm,
    chestCm = chestCm,
    waistCm = waistCm,
    abdomenCm = abdomenCm,
    hipsCm = hipsCm,
    thighCm = thighCm,
    armCm = armCm
)

// PŘEVOD Z MeasurementEntity -> Measurement
fun MeasurementEntity.toMeasurement() = Measurement(
    measurementId = measurementId,
    clientId = clientId,
    date = date,
    weight = weight,
    bustCm = bustCm,
    chestCm = chestCm,
    waistCm = waistCm,
    abdomenCm = abdomenCm,
    hipsCm = hipsCm,
    thighCm = thighCm,
    armCm = armCm
)