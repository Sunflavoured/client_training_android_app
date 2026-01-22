package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.client_training_app.data.database.Converters

@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,  // Uložíme jako String (např. "STRENGTH")
    val description: String?,
    val mediaType: String, // Uložíme jako String (např. "NONE")
    val mediaUrl: String?,
    val muscleGroups: List<String>, // Room to převede pomocí Converters
    val isDefault: Boolean = false,
    val testPoznamka: String? = null
)