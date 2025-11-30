package com.example.client_training_app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "measurements",
    // Cizí klíč zůstává beze změny, správně odkazuje na ClientEntity
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index pro rychlé hledání podle klienta zůstává
    indices = [Index(value = ["clientId"])]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val measurementId: Int = 0,

    // CIZÍ KLÍČ: Odkaz na klienta
    val clientId: String,

    // Datum měření
    val date: Long = System.currentTimeMillis(),

    // --- MĚŘENÍ TĚLESNÉ VÁHY A OBVODY ---

    // Váha (kg)
    val weight: Double?,

    // Obvody v centimetrech (cm) - všechna pole jsou nullable, pro flexibilitu
    val bustCm: Double?,      // Prsa (přes prsa/hrudník)
    val chestCm: Double?,     // Hrudník (pod prsy)
    val waistCm: Double?,     // Pas
    val abdomenCm: Double?,   // Břicho (nejširší místo)
    val hipsCm: Double?,      // Zadek/boky
    val thighCm: Double?,     // Stehno (obvykle neširší místo)
    val armCm: Double?        // Paže (obvykle biceps v relaxaci)
)
