package com.example.client_training_app.ui.active_workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.data.entity.WorkoutSetResultEntity
import com.example.client_training_app.data.repository.ScheduleRepository
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.data.repository.WorkoutRepository // Ujisti se, že se jmenuje takto (nebo WorkoutSessionRepository)
import com.example.client_training_app.model.ActiveExerciseUi
import com.example.client_training_app.model.ActiveSetUi
import kotlinx.coroutines.launch
import java.util.UUID

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val unitRepository = TrainingUnitRepository(application)
    private val workoutRepository = WorkoutRepository(application)
    private val scheduleRepository = ScheduleRepository(application)

    private val _trainingNote = MutableLiveData<String?>()
    val trainingNote: LiveData<String?> = _trainingNote

    // UI State
    private val _activeExercises = MutableLiveData<List<ActiveExerciseUi>>()
    val activeExercises: LiveData<List<ActiveExerciseUi>> = _activeExercises

    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> = _isFinished

    // Metadata pro uložení
    private var trainingStartTime: Long = 0
    private var currentClientId: String = ""
    private var currentTrainingUnitId: String? = null
    private var currentTrainingName: String = ""

    // ID z kalendáře (pokud existuje)
    private var scheduledWorkoutId: Long? = null

    // ID samotného tréninku (Session)
    // Pokud editujeme, bude to ID z databáze. Pokud je nový, vygenerujeme ho.
    private var currentSessionId: String = UUID.randomUUID().toString()

    fun startWorkout(trainingUnitId: String, clientId: String, scheduleId: Long?) {
        trainingStartTime = System.currentTimeMillis()
        currentClientId = clientId
        currentTrainingUnitId = trainingUnitId
        scheduledWorkoutId = scheduleId

        viewModelScope.launch {
            // 1. Zkusíme zjistit, jestli už k tomuto plánu existuje historie (Editace)
            // (Musíš mít v repo metodu getSessionByScheduleId - viz poznámka pod kódem)
            var existingSession: WorkoutSessionEntity? = null

            if (scheduleId != null) {
                // POZOR: Tuto metodu musíš mít v Repository/DAO, pokud ti svítí červeně, viz níže
                existingSession = workoutRepository.getSessionByScheduleId(scheduleId)
            }

            if (existingSession != null) {
                // --- SCÉNÁŘ A: Editace existujícího tréninku ---
                currentSessionId = existingSession.id // Použijeme existující ID pro přepis
                currentTrainingName = existingSession.trainingName
                loadFromHistory(existingSession)
            } else {
                // --- SCÉNÁŘ B: Nový trénink (Šablona) ---
                currentSessionId = UUID.randomUUID().toString() // Nové ID
                loadFromTemplate(trainingUnitId)
            }
        }
    }

    // --- Logika načtení prázdné šablony ---
    private suspend fun loadFromTemplate(unitId: String) {
        val unitWithExercises = unitRepository.getTrainingUnitWithExercises(unitId)

        if (unitWithExercises != null) {
            currentTrainingName = unitWithExercises.trainingUnit.name
            _trainingNote.value = unitWithExercises.trainingUnit.note

            val uiList = unitWithExercises.exercises.map { detail ->
                val template = detail.trainingData
                val targetSets = template.sets.toIntOrNull() ?: 3

                val prefilledSets = MutableList(targetSets) { index ->
                    ActiveSetUi(setNumber = index + 1)
                }

                ActiveExerciseUi(
                    exerciseId = detail.exercise.id,
                    exerciseName = detail.exercise.name,
                    targetNote = buildString {
                        append("🎯 Cíl: ${template.sets}")

                        if (template.isRepsEnabled) append("x ${template.reps ?: "?"} op.")
                        if (template.isTimeEnabled) append("x ${template.time ?: "?"} s")
                        if (template.isDistanceEnabled) append("x ${template.distance ?: "?"} km")
                        if (template.isWeightEnabled && !template.weight.isNullOrEmpty()) {
                            append(" @ ${template.weight} kg") }
                        if (template.isRirEnabled && !template.rir.isNullOrEmpty()) {
                            append(" | RIR ${template.rir}") }
                        if (template.isRestEnabled) append(" | Pauza: ${template.rest ?: "?"} s")
                    },
                    sets = prefilledSets,

                    // Konfigurace
                    isRepsEnabled = template.isRepsEnabled,
                    isWeightEnabled = template.isWeightEnabled,
                    isTimeEnabled = template.isTimeEnabled,
                    isDistanceEnabled = template.isDistanceEnabled,
                    isRirEnabled = template.isRirEnabled
                )
            }
            _activeExercises.value = uiList
        }
    }

    // --- Logika načtení historie (Editace) ---
    private suspend fun loadFromHistory(session: WorkoutSessionEntity) {
        // 1. Načteme hotové série z DB
        // (Metodu getSetsForSession musíš mít v repo)
        val results = workoutRepository.getSetsForSession(session.id)

        // 2. Načteme i šablonu, abychom věděli názvy cviků a konfiguraci (sloupečky)
        val unitWithExercises = unitRepository.getTrainingUnitWithExercises(session.trainingUnitId ?: "")

        if (unitWithExercises != null) {
            _trainingNote.value = unitWithExercises.trainingUnit.note
            // Seskupíme výsledky podle ID cviku, aby se nám lépe hledaly
            val resultsByExercise = results.groupBy { it.exerciseId }

            val uiList = unitWithExercises.exercises.map { detail ->
                val template = detail.trainingData

                // Najdeme hotové série pro tento cvik
                val historySets = resultsByExercise[detail.exercise.id] ?: emptyList()

                // Převedeme DB entity na UI modely
                val activeSets = if (historySets.isNotEmpty()) {
                    historySets.map { res ->
                        ActiveSetUi(
                            setNumber = res.setNumber,
                            weight = res.weight ?: "",
                            reps = res.reps ?: "",
                            time = res.time ?: "",
                            distance = res.distance ?: "",
                            rir = res.rir ?: "",
                            isCompleted = res.isCompleted
                        )
                    }.toMutableList()
                } else {
                    // Pokud v historii pro tento cvik nic není (divné, ale možné), dáme prázdné
                    MutableList(template.sets.toIntOrNull() ?: 3) { ActiveSetUi(setNumber = it + 1) }
                }

                ActiveExerciseUi(
                    exerciseId = detail.exercise.id,
                    exerciseName = detail.exercise.name,
                    targetNote = "Upravit záznam", // Nebo původní note
                    sets = activeSets,

                    // Konfigurace bereme ze šablony
                    isRepsEnabled = template.isRepsEnabled,
                    isWeightEnabled = template.isWeightEnabled,
                    isTimeEnabled = template.isTimeEnabled,
                    isDistanceEnabled = template.isDistanceEnabled,
                    isRirEnabled = template.isRirEnabled
                )
            }
            _activeExercises.value = uiList
        }
    }

    fun addSet(exerciseIndex: Int) {
        val currentList = _activeExercises.value ?: return
        val exercise = currentList[exerciseIndex]
        val newSetNumber = exercise.sets.size + 1
        exercise.sets.add(ActiveSetUi(setNumber = newSetNumber))

        // Vynutíme update (vytvoříme novou referenci listu, aby LiveData zareagovala)
        _activeExercises.value = currentList.toList()
    }

    fun finishWorkout() {
        val exercises = _activeExercises.value ?: return
        val endTime = System.currentTimeMillis()

        viewModelScope.launch {
            // 1. Hlavička (používáme currentSessionId - buď nové, nebo to co editujeme)
            val sessionEntity = WorkoutSessionEntity(
                id = currentSessionId,
                clientId = currentClientId,
                trainingUnitId = currentTrainingUnitId,
                scheduledWorkoutId = scheduledWorkoutId, // <--- Ukládáme vazbu na kalendář
                trainingName = currentTrainingName,
                startTime = trainingStartTime,
                endTime = endTime
            )

            // 2. Výsledky
            val resultEntities = mutableListOf<WorkoutSetResultEntity>()

            exercises.forEach { exerciseUi ->
                exerciseUi.sets.forEach { setUi ->
                    // Ukládáme jen smysluplná data
                    if (setUi.isCompleted || setUi.weight.isNotEmpty() || setUi.reps.isNotEmpty() || setUi.time.isNotEmpty()) {
                        resultEntities.add(WorkoutSetResultEntity(
                            sessionId = currentSessionId,
                            exerciseId = exerciseUi.exerciseId,
                            setNumber = setUi.setNumber,
                            weight = setUi.weight,
                            reps = setUi.reps,
                            time = setUi.time,
                            distance = setUi.distance,
                            rir = setUi.rir,
                            isCompleted = setUi.isCompleted
                        ))
                    }
                }
            }

            // 3. Uložení (DAO používá @Insert(onConflict = REPLACE), takže to funguje i pro update)
            workoutRepository.saveWorkout(sessionEntity, resultEntities)

            // 4. Odškrtnutí v kalendáři
            scheduledWorkoutId?.let { id ->
                scheduleRepository.markAsCompleted(id)
            }

            _isFinished.value = true
        }
    }
}