package com.example.client_training_app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.ExerciseCategory
import com.example.client_training_app.model.MediaType


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
    val isDefault: Boolean = false
)

// Extension funkce pro převod mezi Exercise z Json a ExerciseEntity
fun Exercise.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    category = category.name,  // Převede STRENGTH na "STRENGTH"
    description = description,
    mediaType = mediaType.name,
    mediaUrl = mediaUrl,
    muscleGroups = muscleGroups,
    isDefault = isDefault
)

fun ExerciseEntity.toExercise() = Exercise(
    id = id,
    name = name,
    category = ExerciseCategory.valueOf(category),  // "STRENGTH" -> STRENGTH
    description = description,
    mediaType = MediaType.valueOf(mediaType),
    mediaUrl = mediaUrl,
    muscleGroups = muscleGroups,
    isDefault = isDefault
)

