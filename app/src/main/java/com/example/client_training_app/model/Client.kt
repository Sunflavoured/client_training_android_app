package com.example.client_training_app.model

data class Client(
    val id: String,              // Unikátní ID klienta
    val firstName: String,       // Jméno
    val lastName: String,        // Příjmení
    val age: Int?,               // Věk (nullable, protože není povinný)
    val weight: Double?,         // Váha v kg (nullable)
    val notes: String            // Poznámky trenéra
)