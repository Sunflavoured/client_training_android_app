package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_set_results",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // Smažeš historii tréninku -> smažou se i výsledky
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT // Nemůžeš smazat cvik, pokud už ho někdo cvičil (nebo SET_NULL)
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class WorkoutSetResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: String,      // Odkaz na hlavičku (WorkoutSession)
    val exerciseId: String,     // Odkaz na cvik

    val setNumber: Int,         // Pořadí série (1, 2, 3...)

    // Naměřené hodnoty (Nullable, protože u Planku nemáš váhu atd.)
    val reps: String? = null,   // String, kdyby chtěl napsat "10-12" nebo prostě int 10
    val weight: String? = null,
    val time: String? = null,   // v sekundách
    val distance: String? = null,
    val rir: String? = null,    // Rezerva

    val isCompleted: Boolean = true // Zda byla série skutečně dokončena
)