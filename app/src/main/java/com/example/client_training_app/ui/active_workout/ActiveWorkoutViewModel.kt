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
import com.example.client_training_app.data.repository.WorkoutRepository
import com.example.client_training_app.model.ActiveExerciseUi
import com.example.client_training_app.model.ActiveSetUi
import com.example.client_training_app.model.toExercise
import kotlinx.coroutines.launch
import java.util.UUID

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val unitRepository = TrainingUnitRepository(application)
    private val workoutRepository = WorkoutRepository(application)
    private val scheduleRepository = ScheduleRepository(application)

    // UI State - Seznam cviků k odškrtání
    private val _activeExercises = MutableLiveData<List<ActiveExerciseUi>>()
    val activeExercises: LiveData<List<ActiveExerciseUi>> = _activeExercises

    // Událost: Trénink dokončen (pro navigaci pryč)
    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> = _isFinished

    // Metadata
    private var trainingStartTime: Long = 0
    private var currentClientId: String = ""
    private var currentTrainingUnitId: String? = null
    private var currentTrainingName: String = ""
    private var scheduledWorkoutId: Long? = null // Pokud jdeme z kalendáře

    fun startWorkout(trainingUnitId: String, clientId: String, scheduleId: Long?) {
        trainingStartTime = System.currentTimeMillis()
        currentClientId = clientId
        currentTrainingUnitId = trainingUnitId
        scheduledWorkoutId = scheduleId

        viewModelScope.launch {
            val unitWithExercises = unitRepository.getTrainingUnitWithExercises(trainingUnitId)

            if (unitWithExercises != null) {
                currentTrainingName = unitWithExercises.trainingUnit.name

                // MAGIE: Převedeme šablonu na Active UI modely
                val uiList = unitWithExercises.exercises.map { detail ->
                    val template = detail.trainingData

                    // 1. Zjistíme, kolik sérií bylo v plánu (default 3)
                    val targetSets = template.sets.toIntOrNull() ?: 3

                    // 2. Vytvoříme předvyplněné řádky
                    val prefilledSets = MutableList(targetSets) { index ->
                        ActiveSetUi(
                            setNumber = index + 1,
                            // Můžeme předvyplnit váhu z minula? (To je advanced feature na později)
                            // Pro teď necháme prázdné
                        )
                    }

                    // 3. Sestavíme objekt cviku
                    ActiveExerciseUi(
                        exerciseId = detail.exercise.id,
                        exerciseName = detail.exercise.name,
                        targetNote = "${template.sets}x${template.reps} ${template.weight ?: ""} ${template.rir?.let { "RIR $it" } ?: ""}",
                        sets = prefilledSets,

                        // Konfigurace sloupců
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
    }

    // Uživatel klikl na "+ Přidat sérii"
    fun addSet(exerciseIndex: Int) {
        val currentList = _activeExercises.value ?: return
        val exercise = currentList[exerciseIndex]

        val newSetNumber = exercise.sets.size + 1
        exercise.sets.add(ActiveSetUi(setNumber = newSetNumber))

        // Musíme "šťouchnout" do LiveData, aby se UI překreslilo
        _activeExercises.value = currentList
        // Poznámka: U RecyclerView adaptéru to někdy nestačí, pokud je reference stejná. 
        // Ve Fragmentu to vyřešíme voláním adapter.notifyDataSetChanged() nebo vytvořením nové kopie listu.
    }

    // Uložení tréninku
    fun finishWorkout() {
        val exercises = _activeExercises.value ?: return
        val endTime = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()

        viewModelScope.launch {
            // 1. Vytvoříme Hlavičku (Session)
            val sessionEntity = WorkoutSessionEntity(
                id = sessionId,
                clientId = currentClientId,
                trainingUnitId = currentTrainingUnitId,
                trainingName = currentTrainingName,
                startTime = trainingStartTime,
                endTime = endTime
            )

            // 2. Vytvoříme Výsledky (Sets)
            val resultEntities = mutableListOf<WorkoutSetResultEntity>()

            exercises.forEach { exerciseUi ->
                exerciseUi.sets.forEach { setUi ->
                    // Uložíme jen ty série, kde je alespoň něco vyplněno nebo jsou "hotové"
                    // (Abychom neukládali prázdné řádky, pokud je uživatel přeskočil)
                    if (setUi.isCompleted || setUi.weight.isNotEmpty() || setUi.reps.isNotEmpty() || setUi.time.isNotEmpty()) {

                        resultEntities.add(WorkoutSetResultEntity(
                            sessionId = sessionId,
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

            // 3. Uložíme do DB
            workoutRepository.saveWorkout(sessionEntity, resultEntities)

            // 4. Pokud to byl naplánovaný trénink z kalendáře, označíme ho jako splněný
            scheduledWorkoutId?.let { id ->
                scheduleRepository.markAsCompleted(id)
            }

            _isFinished.value = true
        }
    }
}