package com.example.client_training_app.data.database

import androidx.room.Embedded
import androidx.room.Relation
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity

data class TrainingUnitWithExercises(
    @Embedded val trainingUnit: TrainingUnitEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "trainingUnitId" // Musí odpovídat názvu sloupce v TrainingUnitExerciseEntity
    )
    val exercises: List<TrainingUnitExerciseEntity>
)