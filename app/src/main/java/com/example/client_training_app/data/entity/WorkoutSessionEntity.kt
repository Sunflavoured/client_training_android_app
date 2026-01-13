package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        // Volitelně můžeme odkázat na TrainingUnit, abychom věděli, podle jaké šablony se jelo
        ForeignKey(
            entity = TrainingUnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingUnitId"],
            onDelete = ForeignKey.SET_NULL // Když smažeš šablonu, historie zůstane, jen ztratí odkaz
        )
    ],
    indices = [Index("clientId"), Index("trainingUnitId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String, // UUID generované při startu tréninku

    val clientId: String,
    val trainingUnitId: String?, // Může být null, pokud je to "Freestyle" trénink bez šablony
    val trainingName: String,    // Uložíme si název (např. "Full Body A"), kdyby se šablona smazala

    val startTime: Long,         // Timestamp začátku
    val endTime: Long? = null,   // Timestamp konce (vyplní se až po dokončení)

    val note: String? = null,    // "Bolelo mě koleno"
    val rating: Int? = null      // Hodnocení náročnosti (1-10 RPE nebo hvězdičky)
)