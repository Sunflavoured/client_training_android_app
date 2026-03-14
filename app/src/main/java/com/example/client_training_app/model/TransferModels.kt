package com.example.client_training_app.model

import com.example.client_training_app.data.entity.ExerciseEntity
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import androidx.annotation.Keep

// Toto je ten objekt, který se převede do JSONu
@Keep
data class TrainingUnitExportPackage(
    val unit: TrainingUnitEntity,               // Hlavička (Název, poznámka...)
    val exercisesInUnit: List<TrainingUnitExerciseEntity>, // Vazby (počet sérií, pauzy...)
    val exerciseDefinitions: List<ExerciseEntity> // Samotné cviky (aby se klientovi vytvořily, pokud je nemá)
)