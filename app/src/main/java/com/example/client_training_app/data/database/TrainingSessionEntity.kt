package com.example.client_training_app.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],     // ID v tabulce clients
            childColumns = ["clientId"], // Odkaz v této tabulce
            onDelete = ForeignKey.CASCADE // Smaž tréninky, když smažu klienta
        )
    ],
    indices = [androidx.room.Index("clientId")]
)
data class TrainingSessionEntity(
    @PrimaryKey
    val id: String,          // UUID tréninku
    val clientId: String,    // Komu patří
    val dateInMillis: Long,  // Datum (uložíme jako timestamp půlnoci daného dne)
    val name: String,        // Např. "Nohy - Těžký", "Kruhový trénink"
    val isCompleted: Boolean = false
)