package com.example.client_training_app.ui.training_unit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.TemplateExercise
import com.example.client_training_app.model.toExercise
import kotlinx.coroutines.launch
import java.util.UUID

class TrainingUnitEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrainingUnitRepository(application)

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

    // --- 1. NAČTENÍ DAT (PRO EDITACI) ---
    fun loadUnitData(unitId: String) {
        // Zabráníme zbytečnému znovunačítání, pokud už máme data
        if (editingUnitId == unitId) return

        editingUnitId = unitId

        viewModelScope.launch {
            val unitWithExercises = repository.getTrainingUnitWithExercises(unitId)

            if (unitWithExercises != null) {
                // Uložíme si info o tréninku
                loadedClientId = unitWithExercises.trainingUnit.clientId
                initialUnitName.value = unitWithExercises.trainingUnit.name
                initialUnitNote.value = unitWithExercises.trainingUnit.note ?: ""

                // Převedeme entity z DB na UI modely (TemplateExercise)
                val loadedList = unitWithExercises.exercises.map { detail ->
                    // 'detail' je nyní typu TrainingUnitExerciseDetail

                    TemplateExercise(
                        // K názvu cviku se dostaneš přes .exercise
                        exercise = detail.exercise.toExercise(),

                        // K sériím/opakováním se dostaneš přes .trainingData
                        order = detail.trainingData.orderIndex,

                        //základní
                        sets = detail.trainingData.sets ?: "3",
                        reps = detail.trainingData.reps ?: "10",
                        weight = detail.trainingData.weight,

                        // extra
                        time = detail.trainingData.time,
                        distance = detail.trainingData.distance,
                        rir = detail.trainingData.rir,
                        rest = detail.trainingData.rest,



                        // A stejně tak flagy:
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



    // --- 2. MANIPULACE SE SEZNAMEM ---

    fun addExercise(exercise: Exercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        val newOrder = currentList.size + 1

        val newItem = TemplateExercise(
            exercise = exercise,
            order = newOrder,
            sets = "4", // Defaultní hodnoty
            reps = "10",
            weight = null,
            rest = "90"
        )
        currentList.add(newItem)
        _templateExercises.value = currentList
    }

    fun updateTemplateExercise(updatedItem: TemplateExercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        // Najdeme index položky a nahradíme ji
        val index = currentList.indexOfFirst { it.exercise.id == updatedItem.exercise.id && it.order == updatedItem.order }

        if (index != -1) {
            currentList[index] = updatedItem
            _templateExercises.value = currentList
        }
    }

    fun deleteTemplateExercise(itemToDelete: TemplateExercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        currentList.remove(itemToDelete)

        // Přečíslování pořadí po smazání
        val reorderedList = currentList.mapIndexed { index, item ->
            item.copy(order = index + 1)
        }
        _templateExercises.value = reorderedList
    }

    // --- 3. ULOŽENÍ ---

    fun saveTrainingUnit(name: String, note: String?, argClientId: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Rozhodneme, jaké ID použít (existující při editaci, nové při vytvoření)
            val finalId = editingUnitId ?: UUID.randomUUID().toString()

            // Rozhodneme, jaké clientId použít (z databáze při editaci, z argumentů při novém)
            val finalClientId = if (editingUnitId != null) loadedClientId else argClientId

            // 1. Vytvoření Entity Tréninku
            val unitEntity = TrainingUnitEntity(
                id = finalId,
                name = name,
                note = note,
                clientId = finalClientId
            )

            // 2. Vytvoření Entit Cviků (Vazební tabulka)
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

                    // Konfigurace taky bereme z itemu:
                    isRepsEnabled = item.isRepsEnabled,
                    isWeightEnabled = item.isWeightEnabled,
                    isTimeEnabled = item.isTimeEnabled,
                    isDistanceEnabled = item.isDistanceEnabled,
                    isRirEnabled = item.isRirEnabled,
                    isRestEnabled = item.isRestEnabled
                )
            }

            // 3. Volání Repository (Upsert transakce)
            repository.updateTrainingUnit(unitEntity, exerciseEntities)

            onSuccess()
        }
    }
    //smazání tréninkové jednotky
    fun deleteTrainingUnit(unit: TrainingUnitEntity) {
        viewModelScope.launch {
            repository.deleteTrainingUnit(unit.id)
            // Seznam se aktualizuje sám díky Flow
        }
    }

    // --- 5. OBNOVENÍ (Pro Undo tlačítko) ---

    fun restoreTrainingUnit(unit: TrainingUnitEntity) {
        viewModelScope.launch {
            // POZOR: Pokud při deleteTrainingUnit došlo k smazání cviků (Cascade),
            // tento příkaz obnoví pouze hlavičku tréninku, ale bude prázdný (bez cviků).
            // Pro plnohodnotné Undo bys musel před smazáním načíst i seznam cviků
            // a v této metodě je uložit zpátky.

            // Pro teď jen vložíme zpět jednotku (aby aplikace nespadla):
            //repository.createTrainingUnit(unit) TODO
        }
    }
}