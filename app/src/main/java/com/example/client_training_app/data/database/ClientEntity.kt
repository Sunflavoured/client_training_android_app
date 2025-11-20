package com.example.client_training_app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.client_training_app.model.Client
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int?,
    val weight: Double?,
    val notes: String
)

// Extension funkce pro převod Client <-> ClientEntity
fun Client.toEntity() = ClientEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    age = age,
    weight = weight,
    notes = notes
)

fun ClientEntity.toClient() = Client(
    id = id,
    firstName = firstName,
    lastName = lastName,
    age = age,
    weight = weight,
    notes = notes
)