package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_units",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("clientId")]
)
data class TrainingUnitEntity(
    @PrimaryKey
    val id: String,

    val name: String,        // Např. "Záda a Biceps A"
    val note: String? = null,

    // Null = Globální jednotka, Vyplněno = Jednotka konkrétního klienta
    val clientId: String? = null
)