package com.example.client_training_app.data.entity // Zkontroluj si svůj package name

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_unit_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingUnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingUnitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trainingUnitId"), Index("exerciseId")]
)
data class TrainingUnitExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val trainingUnitId: String,
    val exerciseId: String,
    val orderIndex: Int, // Pořadí v seznamu

    // --- DATA ---
    val sets: String,
    val reps: String?,
    val weight: String?,
    val time: String?,      // Nové
    val distance: String?,  // Nové
    val rir: String?,       // Nové
    val rest: String?,

    // --- KONFIGURACE (Booleans) ---
    val isRepsEnabled: Boolean,
    val isWeightEnabled: Boolean,
    val isTimeEnabled: Boolean,
    val isDistanceEnabled: Boolean,
    val isRirEnabled: Boolean,
    val isRestEnabled: Boolean
)