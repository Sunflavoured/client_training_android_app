package com.example.client_training_app

data class Exercise (
    val id: String,                    // Unikátní ID (např. "squat_01")
    val name: String,                  // Název cviku
    val category: ExerciseCategory,    // Kardio/Síla/Mobilita
    val description: String,           // Popis provedení
    val mediaType: MediaType,          // Typ média (Žádné/Obrázek/YouTube)
    val mediaUrl: String?,             // URL na obrázek nebo YouTube video,nullable (String?), protože cvik nemusí mít médium
    val muscleGroups: List<String>,    // Seznam svalových skupin
    val isDefault: Boolean = false     // True = z JSON, nelze smazat
    )

// Enum pro kategorii cviku - zajistí, že můžu vybírat pouze z těchto možností
enum class ExerciseCategory(val displayName: String) {
    CARDIO("Kardio"),
    STRENGTH("Síla"),
    MOBILITY("Mobilita")
}

// Enum pro typ média
enum class MediaType {
    NONE,           // Žádné médium
    IMAGE_URL,      // URL odkaz na obrázek
    IMAGE_GALLERY,  // Obrázek z galerie telefonu
    YOUTUBE         // YouTube video URL
}