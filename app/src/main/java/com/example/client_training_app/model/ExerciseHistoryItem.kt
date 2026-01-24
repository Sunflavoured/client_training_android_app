package com.example.client_training_app.model

data class ExerciseHistoryItem(
    val sessionId: String,
    val startTime: Long,     // Datum tréninku
    val setNumber: Int,
    val reps: String?,
    val weight: String?,
    val time: String?,
    val distance: String?,
    val rir: String?,
    val rest: String?,
    val isCompleted: Boolean
)