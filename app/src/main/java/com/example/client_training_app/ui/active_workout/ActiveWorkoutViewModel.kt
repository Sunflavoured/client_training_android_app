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
    private var currentClientId: String = ""
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
                    isRirEnabled = template.isRirEnabled
                )
            }
            _activeExercises.value = uiList
        }
    }

    // --- Logika načtení historie (Editace) ---
    private suspend fun loadFromHistory(session: WorkoutSessionEntity) {
        val results = workoutRepository.getSetsForSession(session.id)
        val unitWithExercises = unitRepository.getTrainingUnitWithExercises(session.trainingUnitId ?: "")

        if (unitWithExercises != null) {
            _trainingNote.value = unitWithExercises.trainingUnit.note
            val resultsByExercise = results.groupBy { it.exerciseId }

            // 1. Nejprve si připravíme seznam podle šablony (zatím bez extra cviků)
            val templateExerciseIds = unitWithExercises.exercises.map { it.exercise.id }.toSet()

            // Tento list budeme postupně upravovat
            val finalUiList = unitWithExercises.exercises.map { detail ->
                val template = detail.trainingData
                val historySets = resultsByExercise[detail.exercise.id] ?: emptyList()

                // Pokud máme historii, použijeme ji. Jinak vytvoříme prázdné chlívky.
                val activeSets = if (historySets.isNotEmpty()) {
                    mapHistoryToUi(historySets)
                } else {
                    MutableList(template.sets.toIntOrNull() ?: 3) { ActiveSetUi(setNumber = it + 1) }
                }

                // Vytvoříme objekt (zatím původní cvik ze šablony)
                ActiveExerciseUi(
                    exerciseId = detail.exercise.id,
                    exerciseName = detail.exercise.name,
                    targetNote = buildTargetString(template),
                    sets = activeSets,
                    // Konfigurace
                    isRepsEnabled = template.isRepsEnabled,
                    isWeightEnabled = template.isWeightEnabled,
                    isTimeEnabled = template.isTimeEnabled,
                    isDistanceEnabled = template.isDistanceEnabled,
                    isRirEnabled = template.isRirEnabled
                )
            }.toMutableList()

            // 2. Najdeme "Extra" cviky (Ty, co jsou v historii, ale ne v šabloně)
            val extraExerciseIds = resultsByExercise.keys.filter { !templateExerciseIds.contains(it) }.toMutableList()

            // 3. LOGIKA SUBSTITUCE: Zkusíme napárovat Extra cviky na Prázdná místa v šabloně
            // Projdeme seznam a hledáme místa, kde nic není vyplněno
            for (i in finalUiList.indices) {
                // Pokud už nemáme žádné extra cviky na rozdání, končíme
                if (extraExerciseIds.isEmpty()) break

                val item = finalUiList[i]

                // Je tento cvik v šabloně "prázdný"? (Tj. žádná historie pro jeho ID)
                val isSkippedInHistory = resultsByExercise[item.exerciseId] == null

                if (isSkippedInHistory) {
                    // MÁME SHODU: Prázdné místo v šabloně + Máme extra cvik "v ruce"
                    val extraId = extraExerciseIds.removeAt(0) // Vezmeme první extra cvik
                    val extraSets = resultsByExercise[extraId] ?: emptyList()
                    val extraDef = exerciseRepository.getExerciseById(extraId)

                    if (extraDef != null) {
                        // Vytvoříme nový UI prvek (Ten nahrazující cvik)
                        val newItem = item.copy(
                            exerciseId = extraDef.id,
                            exerciseName = extraDef.name,
                            targetNote = "Nahrazeno: ${item.exerciseName}", // Ponecháme info o původním cviku
                            sets = mapHistoryToUi(extraSets),
                            // U náhradního cviku nevíme config, necháme původní nebo zapneme vše
                            isRepsEnabled = true,
                            isWeightEnabled = true
                        )
                        // Přepíšeme původní prázdný cvik tímto novým
                        finalUiList[i] = newItem
                    }
                }
            }

            // 4. Pokud zbyly ještě nějaké Extra cviky (které se nevešly do náhrad), přidáme je nakonec
            for (extraId in extraExerciseIds) {
                val extraSets = resultsByExercise[extraId] ?: emptyList()
                val extraDef = exerciseRepository.getExerciseById(extraId)

                if (extraDef != null) {
                    val extraItem = ActiveExerciseUi(
                        exerciseId = extraDef.id,
                        exerciseName = extraDef.name,
                        targetNote = "Cvik navíc",
                        sets = mapHistoryToUi(extraSets),
                        isRepsEnabled = true,
                        isWeightEnabled = true,
                        isTimeEnabled = false,
                        isDistanceEnabled = false,
                        isRirEnabled = true
                    )
                    finalUiList.add(extraItem)
                }
            }

            _activeExercises.value = finalUiList
        }
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
            isRirEnabled = true
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