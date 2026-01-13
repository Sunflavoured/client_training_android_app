package com.example.client_training_app.data.database

import androidx.room.Embedded
import androidx.room.Relation
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity

data class TrainingUnitWithExercises(
    // Hlavička tréninku (Název tréninku, poznámka...)
    @Embedded val trainingUnit: TrainingUnitEntity,

    // Seznam cviků v tomto tréninku
    @Relation(
        entity = TrainingUnitExerciseEntity::class, // Říkáme Roomu: "Hledej ve vazební tabulce"
        parentColumn = "id",            // ID tréninku
        entityColumn = "trainingUnitId" // Sloupec v tabulce training_unit_exercises
    )
    // Vracíme seznam detailů (kde jsou i série), ne jen holé cviky
    val exercises: List<TrainingUnitExerciseDetail>
)