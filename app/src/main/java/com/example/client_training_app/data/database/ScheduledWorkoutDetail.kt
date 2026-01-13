package com.example.client_training_app.data.database

import androidx.room.Embedded
import androidx.room.Relation
import com.example.client_training_app.data.entity.ScheduledWorkoutEntity
import com.example.client_training_app.data.entity.TrainingUnitEntity

// Toto je objekt, který budeme posílat do Kalendáře
data class ScheduledWorkoutDetail(
    @Embedded val schedule: ScheduledWorkoutEntity,

    @Relation(
        parentColumn = "trainingUnitId",
        entityColumn = "id"
    )
    val trainingUnit: TrainingUnitEntity
)