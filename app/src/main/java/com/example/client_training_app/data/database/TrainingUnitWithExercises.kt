package com.example.client_training_app.data.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.client_training_app.data.entity.ExerciseEntity
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity

data class TrainingUnitWithExercises(
    @Embedded val trainingUnit: TrainingUnitEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TrainingUnitExerciseEntity::class,
            parentColumn = "trainingUnitId",
            entityColumn = "exerciseId"
        )
    )

    val exercises: List<ExerciseEntity>
)