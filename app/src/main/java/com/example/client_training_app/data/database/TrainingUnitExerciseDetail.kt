package com.example.client_training_app.data.database

import androidx.room.Embedded
import androidx.room.Relation
import com.example.client_training_app.data.entity.ExerciseEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity

data class TrainingUnitExerciseDetail(
    // 1. Zde jsou data o tréninku (série, opakování, čas...)
    // @Embedded "rozbalí" sloupečky z TrainingUnitExerciseEntity přímo sem
    @Embedded
    val trainingData: TrainingUnitExerciseEntity,

    // 2. K těmto datům "přilepíme" detaily o cviku (název, obrázek)
    @Relation(
        parentColumn = "exerciseId", // ID ve vazební tabulce (trainingData)
        entityColumn = "id"          // ID v tabulce cviků
    )
    val exercise: ExerciseEntity
)