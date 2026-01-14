package com.example.client_training_app.model

// Reprezentuje jeden CVIK na obrazovce
data class ActiveExerciseUi(
    val exerciseId: String,
    val exerciseName: String,
    val targetNote: String?, // Např. "3x10, RIR 2" (to co bylo v plánu)
    val sets: MutableList<ActiveSetUi>, // Seznam sérií

    val isRepsEnabled: Boolean,
    val isWeightEnabled: Boolean,
    val isTimeEnabled: Boolean,
    val isDistanceEnabled: Boolean,
    val isRirEnabled: Boolean
)

// Reprezentuje jeden ŘÁDEK (Sérii)
data class ActiveSetUi(
    val setNumber: Int,
    var weight: String = "",   // Co uživatel napsal
    var reps: String = "",
    var rir: String = "",
    var time: String = "",
    var distance: String = "",
    var isCompleted: Boolean = false // Zaškrtávátko
)