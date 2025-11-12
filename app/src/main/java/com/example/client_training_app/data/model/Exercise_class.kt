package com.example.client_training_app.data.model

data class Exercise (
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val description: String,
    val mediaType: MediaType,
    val mediaUrl: String?,
    val muscleGroups: List<String>,    // Zůstane List<String> pro flexibilitu
    val isDefault: Boolean = false
)

// Enum pro kategorii cviku
enum class ExerciseCategory(val displayName: String) {
    CARDIO("Kardio"),
    STRENGTH("Síla"),
    MOBILITY("Mobilita")
}

// Enum pro typ média
enum class MediaType {
    NONE,
    IMAGE_URL,
    IMAGE_GALLERY,
    YOUTUBE
}

// ENUM pro svalové skupiny
enum class MuscleGroup(val displayName: String) {
    CHEST("Hrudník"),
    SHOULDERS("Ramena"),
    LOWER_BACK("Spodní záda"),
    RHOMBOIDS("Rhombický sval"),
    LATS("Latissimus"),
    QUADS("Přední stehna"),
    HAMSTRINGS("Zadní stehna"),
    ABS("Břicho"),
    CALVES("Lýtka"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    GLUTES("Gluteus");

    companion object {
        // Helper funkce pro získání všech názvů
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }

        // Helper pro převod displayName -> enum
        fun fromDisplayName(displayName: String): MuscleGroup? {
            return values().find { it.displayName == displayName }
        }
    }
}