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
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import com.example.client_training_app.data.repository.ExerciseRepository
import com.example.client_training_app.data.repository.WorkoutRepository
import com.example.client_training_app.model.ActiveExerciseUi
import com.example.client_training_app.model.ActiveSetUi
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Collections // Import pro swap

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private var isDataLoaded = false
    private val unitRepository = TrainingUnitRepository(application)
    private val workoutRepository = WorkoutRepository(application)
    private val scheduleRepository = ScheduleRepository(application)

    private val exerciseRepository = ExerciseRepository(application)

    private val _trainingNote = MutableLiveData<String?>()
    val trainingNote: LiveData<String?> = _trainingNote

    // UI State
    private val _activeExercises = MutableLiveData<List<ActiveExerciseUi>>()
    val activeExercises: LiveData<List<ActiveExerciseUi>> = _activeExercises

    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> = _isFinished

    // Metadata pro uložení
    private var trainingStartTime: Long = 0
    var currentClientId: String = ""
    private var currentTrainingUnitId: String? = null
    private var currentTrainingName: String = ""

    // ID z kalendáře (pokud existuje)
    private var scheduledWorkoutId: Long? = null

    // ID samotného tréninku (Session)
    private var currentSessionId: String = UUID.randomUUID().toString()



    fun startWorkout(trainingUnitId: String, clientId: String, scheduleId: Long?) {
        trainingStartTime = System.currentTimeMillis()
        currentClientId = clientId
        currentTrainingUnitId = trainingUnitId
        scheduledWorkoutId = scheduleId

        if (isDataLoaded) {
            return // Pokud už máme data, nic nedělej a nečti znovu z DB
        }
        isDataLoaded = true

        viewModelScope.launch {
            var existingSession: WorkoutSessionEntity? = null

            if (scheduleId != null) {
                existingSession = workoutRepository.getSessionByScheduleId(scheduleId)
            }

            if (existingSession != null) {
                // --- SCÉNÁŘ A: Editace existujícího tréninku ---
                currentSessionId = existingSession.id
                currentTrainingName = existingSession.trainingName
                loadFromHistory(existingSession)
            } else {
                // --- SCÉNÁŘ B: Nový trénink (Šablona) ---
                currentSessionId = UUID.randomUUID().toString()
                loadFromTemplate(trainingUnitId)
            }
        }
    }
    // Pomocná funkce - vyrobí text "Cíl: 4 x 10 op. @ 50kg..."
    private fun buildTargetString(template: TrainingUnitExerciseEntity): String {
        return buildString {
            append("Cíl: ${template.sets}")
            if (template.isRepsEnabled) append(" x ${template.reps ?: "?"} op.")
            if (template.isTimeEnabled) append(" x ${template.time ?: "?"} s")
            if (template.isDistanceEnabled) append(" x ${template.distance ?: "?"} km")
            if (template.isWeightEnabled && !template.weight.isNullOrEmpty()) {
                append(" @ ${template.weight} kg")
            }
            if (template.isRirEnabled && !template.rir.isNullOrEmpty()) {
                append(" (RIR ${template.rir})")
            }
            if (template.isRestEnabled) append(" | Pauza: ${template.rest} s")
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

                    targetNote = buildTargetString(template),

                    sets = prefilledSets,
                    isRepsEnabled = template.isRepsEnabled,
                    isWeightEnabled = template.isWeightEnabled,
                    isTimeEnabled = template.isTimeEnabled,
                    isDistanceEnabled = template.isDistanceEnabled,
                    isRirEnabled = template.isRirEnabled,
                    isRestEnabled = template.isRestEnabled
                )
            }
            _activeExercises.value = uiList
        }
    }

    // --- Logika načtení historie (odcvičený trénink) ---
    // Odcvičený trénink je záznam o tom, co se skutečně stalo. Zobrazujeme proto jen uložené série
    // a šablonu nečteme: pozdější úprava šablony (přidaný, odebraný nebo změněný cvik) se do něj nepromítá.
    private suspend fun loadFromHistory(session: WorkoutSessionEntity) {
        val results = workoutRepository.getSetsForSession(session.id)
        _trainingNote.value = session.note

        // Pořadí cviků = pořadí, v jakém byly uloženy (série se ukládají v pořadí z obrazovky)
        val resultsByExercise = results.sortedBy { it.id }.groupBy { it.exerciseId }

        val uiList = resultsByExercise.mapNotNull { (exerciseId, sets) ->
            val definition = exerciseRepository.getExerciseById(exerciseId) ?: return@mapNotNull null

            // Které údaje se sledovaly, poznáme podle toho, co je u cviku vyplněno
            val hasReps = sets.any { !it.reps.isNullOrBlank() }
            val hasWeight = sets.any { !it.weight.isNullOrBlank() }
            val hasTime = sets.any { !it.time.isNullOrBlank() }
            val hasDistance = sets.any { !it.distance.isNullOrBlank() }
            val hasRir = sets.any { !it.rir.isNullOrBlank() }
            val hasRest = sets.any { !it.rest.isNullOrBlank() }
            // Když nebylo vyplněno nic (jen odškrtnuté série), nabídneme opakování a váhu
            val nothingFilled = !(hasReps || hasWeight || hasTime || hasDistance || hasRir || hasRest)

            ActiveExerciseUi(
                exerciseId = definition.id,
                exerciseName = definition.name,
                targetNote = "Zapsáno v tréninku",
                sets = mapHistoryToUi(sets.sortedBy { it.setNumber }),
                isRepsEnabled = hasReps || nothingFilled,
                isWeightEnabled = hasWeight || nothingFilled,
                isTimeEnabled = hasTime,
                isDistanceEnabled = hasDistance,
                isRirEnabled = hasRir,
                isRestEnabled = hasRest
            )
        }

        if (uiList.isEmpty()) {
            // Trénink byl uložen bez jediné vyplněné série: nabídneme aktuální šablonu, ať je co vyplnit
            loadFromTemplate(session.trainingUnitId ?: currentTrainingUnitId ?: "")
            return
        }
        _activeExercises.value = uiList
    }

    // Pomocná metoda pro převod DB výsledků na UI (aby se kód neopakoval)
    private fun mapHistoryToUi(historySets: List<WorkoutSetResultEntity>): MutableList<ActiveSetUi> {
        return historySets.map { res ->
            ActiveSetUi(
                setNumber = res.setNumber,
                weight = res.weight ?: "",
                reps = res.reps ?: "",
                time = res.time ?: "",
                distance = res.distance ?: "",
                rir = res.rir ?: "",
                rest = res.rest ?: "",
                isCompleted = res.isCompleted
            )
        }.toMutableList()
    }

    fun addSet(exerciseIndex: Int) {
        val currentList = _activeExercises.value ?: return
        val exercise = currentList[exerciseIndex]
        val newSetNumber = exercise.sets.size + 1
        exercise.sets.add(ActiveSetUi(setNumber = newSetNumber))
        _activeExercises.value = currentList.toList()
    }

    // --- NOVÉ FUNKCE PRO ÚPRAVU TRÉNINKU ZA BĚHU ---

    fun addNewExercise(exercise: com.example.client_training_app.model.Exercise) {
        val currentList = _activeExercises.value?.toMutableList() ?: return
        val newSets = MutableList(3) { ActiveSetUi(setNumber = it + 1) }

        val newActiveExercise = ActiveExerciseUi(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            targetNote = "Extra cvik",
            sets = newSets,
            isRepsEnabled = true,
            isWeightEnabled = true,
            isTimeEnabled = false,
            isDistanceEnabled = false,
            isRirEnabled = true,
            isRestEnabled = false
        )

        currentList.add(newActiveExercise)
        _activeExercises.value = currentList.toList()
    }

    fun substituteExercise(oldExerciseIndex: Int, newExercise: com.example.client_training_app.model.Exercise) {
        val currentList = _activeExercises.value?.toMutableList() ?: return
        if (oldExerciseIndex !in currentList.indices) return

        val oldItem = currentList[oldExerciseIndex]
        // Resetujeme hodnoty, ale necháme počet sérií
        val newSets = oldItem.sets.map { it.copy(weight = "", reps = "", time = "", distance = "") }.toMutableList()

        val newItem = oldItem.copy(
            exerciseId = newExercise.id,
            exerciseName = newExercise.name,
            targetNote = "Nahrazeno: ${oldItem.exerciseName}",
            sets = newSets
        )

        currentList[oldExerciseIndex] = newItem
        _activeExercises.value = currentList.toList()
    }

    fun moveExercise(fromPosition: Int, toPosition: Int) {
        val currentList = _activeExercises.value?.toMutableList() ?: return
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(currentList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(currentList, i, i - 1)
            }
        }
        _activeExercises.value = currentList.toList()
    }

    fun finishWorkout() {
        val exercises = _activeExercises.value ?: return
        val endTime = System.currentTimeMillis()

        viewModelScope.launch {
             val sessionEntity = WorkoutSessionEntity(
                id = currentSessionId,
                clientId = currentClientId,
                trainingUnitId = currentTrainingUnitId,
                scheduledWorkoutId = scheduledWorkoutId,
                trainingName = currentTrainingName,
                startTime = trainingStartTime,
                endTime = endTime
            )

            val resultEntities = mutableListOf<WorkoutSetResultEntity>()

            exercises.forEach { exerciseUi ->
                exerciseUi.sets.forEach { setUi ->
                    // Ukládáme jen pokud je něco vyplněno nebo odškrtnuto
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

            workoutRepository.saveWorkout(sessionEntity, resultEntities)

            scheduledWorkoutId?.let { id ->
                scheduleRepository.markAsCompleted(id)
            }

            _isFinished.value = true
        }
    }
}