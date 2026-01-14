package com.example.client_training_app.ui.training_unit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData // Důležitý import pro převod Flow na LiveData
import androidx.lifecycle.viewModelScope
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import com.example.client_training_app.data.repository.ClientRepository
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.TemplateExercise
import com.example.client_training_app.model.toExercise
import kotlinx.coroutines.launch
import java.util.UUID

class TrainingUnitEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrainingUnitRepository(application)
    // 1. Inicializace ClientRepository (stejně jako u TrainingUnitRepository)
    private val clientRepository = ClientRepository(application)

    // ID tréninku, který editujeme (null = vytváříme nový)
    private var editingUnitId: String? = null

    // Původní clientId (pokud editujeme existující trénink, musíme ho zachovat)
    private var loadedClientId: String? = null

    // Živý seznam cviků v editoru
    private val _templateExercises = MutableLiveData<List<TemplateExercise>>(emptyList())
    val templateExercises: LiveData<List<TemplateExercise>> = _templateExercises

    // LiveData pro předvyplnění políček (jen při načtení)
    val initialUnitName = MutableLiveData<String>()
    val initialUnitNote = MutableLiveData<String>()

    // 2. Načtení klientů převedené na LiveData pro UI
    // Předpokládá, že v ClientRepository máš metodu getAllClientsFlow() vracející Flow<List<ClientEntity>>
    val clients = clientRepository.getAllClientsFlow().asLiveData()

    // Vybraný klient v roletce (null = Globální)
    val selectedClientId = MutableLiveData<String?>(null)

    // --- 1. NAČTENÍ DAT (PRO EDITACI) ---
    fun loadUnitData(unitId: String) {
        if (editingUnitId == unitId) return

        editingUnitId = unitId

        viewModelScope.launch {
            val unitWithExercises = repository.getTrainingUnitWithExercises(unitId)

            if (unitWithExercises != null) {
                loadedClientId = unitWithExercises.trainingUnit.clientId

                // 3. Nastavíme vybraného klienta v UI podle načtených dat
                selectedClientId.value = loadedClientId

                initialUnitName.value = unitWithExercises.trainingUnit.name
                initialUnitNote.value = unitWithExercises.trainingUnit.note ?: ""

                val loadedList = unitWithExercises.exercises.map { detail ->
                    TemplateExercise(
                        exercise = detail.exercise.toExercise(),
                        order = detail.trainingData.orderIndex,
                        sets = detail.trainingData.sets ?: "3",
                        reps = detail.trainingData.reps ?: "10",
                        weight = detail.trainingData.weight,
                        time = detail.trainingData.time,
                        distance = detail.trainingData.distance,
                        rir = detail.trainingData.rir,
                        rest = detail.trainingData.rest,

                        isRepsEnabled = detail.trainingData.isRepsEnabled,
                        isWeightEnabled = detail.trainingData.isWeightEnabled,
                        isTimeEnabled = detail.trainingData.isTimeEnabled,
                        isDistanceEnabled = detail.trainingData.isDistanceEnabled,
                        isRirEnabled = detail.trainingData.isRirEnabled,
                        isRestEnabled = detail.trainingData.isRestEnabled
                    )
                }.sortedBy { it.order }

                _templateExercises.value = loadedList
            }
        }
    }

    // --- 2. MANIPULACE SE SEZNAMEM --- (Zůstává beze změny)

    fun addExercise(exercise: Exercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        val newOrder = currentList.size + 1

        val newItem = TemplateExercise(
            exercise = exercise,
            order = newOrder,
            sets = "4",
            reps = "10",
            weight = null,
            rest = "90"
        )
        currentList.add(newItem)
        _templateExercises.value = currentList
    }

    fun updateTemplateExercise(updatedItem: TemplateExercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.exercise.id == updatedItem.exercise.id && it.order == updatedItem.order }

        if (index != -1) {
            currentList[index] = updatedItem
            _templateExercises.value = currentList
        }
    }

    fun deleteTemplateExercise(itemToDelete: TemplateExercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        currentList.remove(itemToDelete)

        val reorderedList = currentList.mapIndexed { index, item ->
            item.copy(order = index + 1)
        }
        _templateExercises.value = reorderedList
    }

    // --- 3. ULOŽENÍ ---

    // Zde už nepotřebujeme argClientId, protože bereme hodnotu z selectedClientId LiveData
    fun saveTrainingUnit(name: String, note: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val finalId = editingUnitId ?: UUID.randomUUID().toString()

            // 4. Použijeme hodnotu z LiveData (z roletky)
            val finalClientId = selectedClientId.value

            val unitEntity = TrainingUnitEntity(
                id = finalId,
                name = name,
                note = note,
                clientId = finalClientId
            )

            val currentExercises = _templateExercises.value ?: emptyList()
            val exerciseEntities = currentExercises.map { item ->
                TrainingUnitExerciseEntity(
                    trainingUnitId = finalId,
                    exerciseId = item.exercise.id,
                    orderIndex = item.order,
                    sets = item.sets,
                    reps = item.reps,
                    weight = item.weight,
                    rest = item.rest,
                    time = item.time,
                    distance = item.distance,
                    rir = item.rir,
                    isRepsEnabled = item.isRepsEnabled,
                    isWeightEnabled = item.isWeightEnabled,
                    isTimeEnabled = item.isTimeEnabled,
                    isDistanceEnabled = item.isDistanceEnabled,
                    isRirEnabled = item.isRirEnabled,
                    isRestEnabled = item.isRestEnabled
                )
            }

            repository.updateTrainingUnit(unitEntity, exerciseEntities)
            onSuccess()
        }
    }

    fun deleteTrainingUnit(unit: TrainingUnitEntity) {
        viewModelScope.launch {
            repository.deleteTrainingUnit(unit.id)
        }
    }
}