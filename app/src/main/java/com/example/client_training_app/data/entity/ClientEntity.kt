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
    val birthDate: Long?, // Nové: Místo věku
    val email: String?,    // Nové: Email
    val phone: String?,    // Nové: Telefon
    val notes: String?     // Zajištění, že Notes je nullable
    // Váha (weight) je ZDE odstraněna.
)
// PŘEVOD Z Client -> ClientEntity
fun Client.toEntity() = ClientEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    // Mapujeme birthDate
    birthDate = birthDate,
    email = email,
    phone = phone,
    notes = notes
)

// PŘEVOD Z ClientEntity -> Client
fun ClientEntity.toClient() = Client(
    id = id,
    firstName = firstName,
    lastName = lastName,
    // Mapujeme birthDate
    birthDate = birthDate,
    email = email,
    phone = phone,
    notes = notes
)