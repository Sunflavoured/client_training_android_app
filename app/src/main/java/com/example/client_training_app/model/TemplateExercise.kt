package com.example.client_training_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TemplateExercise(
    val exercise: Exercise,
    val order: Int = 0,

    // --- HODNOTY (Data) ---
    val sets: String = "3",
    val reps: String? = null,    // Nullable, protože u běhu opáčka nechceme
    val weight: String? = null,
    val time: String? = null,    // Nové: Čas (např. v sekundách nebo "1:30")
    val distance: String? = null,// Nové: Vzdálenost (metry/km)
    val rir: String? = null,     // Nové: Reps In Reserve
    val rest: String? = null,

    // --- KONFIGURACE (Co sledovat?) ---
    // Defaultně zapneme Váhu a Opakování (klasická síla)
    val isRepsEnabled: Boolean = true,
    val isWeightEnabled: Boolean = true,
    val isTimeEnabled: Boolean = false,
    val isDistanceEnabled: Boolean = false,
    val isRirEnabled: Boolean = false,
    val isRestEnabled: Boolean = false
) : Parcelable