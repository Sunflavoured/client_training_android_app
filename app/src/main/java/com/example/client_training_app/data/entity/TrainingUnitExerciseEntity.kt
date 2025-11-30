package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_unit_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingUnitEntity::class, // Odkazujeme na novou entitu
            parentColumns = ["id"],
            childColumns = ["trainingUnitId"],  // Přejmenovaný sloupec
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("trainingUnitId"), Index("exerciseId")]
)
data class TrainingUnitExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val trainingUnitId: String, // ID Tréninkové jednotky
    val exerciseId: String,     // ID Cviku

    val sets: Int,
    val reps: String,
    val weight: Double?,
    val restSeconds: Int?
)