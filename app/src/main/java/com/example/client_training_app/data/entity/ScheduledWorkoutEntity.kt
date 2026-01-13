package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_workouts",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class, // Odkaz na Klienta
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE // Smažeš klienta -> smažou se i plány
        ),
        ForeignKey(
            entity = TrainingUnitEntity::class, // Odkaz na Šablonu tréninku
            parentColumns = ["id"],
            childColumns = ["trainingUnitId"],
            onDelete = ForeignKey.CASCADE // Smažeš šablonu -> smaže se z kalendáře
        )
    ],
    indices = [
        Index("clientId"),
        Index("trainingUnitId"),
        Index("date") // Index pro rychlé hledání v kalendáři
    ]
)
data class ScheduledWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clientId: String,       // Kdo
    val trainingUnitId: String, // Co
    val date: Long,             // Kdy (Unix timestamp - ideálně půlnoc daného dne)

    val isCompleted: Boolean = false, // Zda už bylo odcviceno (pro barvičku v kalendáři - zelená/šedá)
    val note: String? = null    // Specifická poznámka pro tento den (např. "Dneska to šetři")
)