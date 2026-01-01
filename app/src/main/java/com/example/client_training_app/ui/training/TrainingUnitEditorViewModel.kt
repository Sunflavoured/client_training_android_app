package com.example.client_training_app.ui.training

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.TemplateExercise
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity

class TrainingUnitEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrainingUnitRepository(application)

    // Živý seznam cviků v editoru (toto sleduje Fragment)
    private val _templateExercises = MutableLiveData<List<TemplateExercise>>(emptyList())
    val templateExercises: LiveData<List<TemplateExercise>> = _templateExercises

    // --- LOGIKA SEZNAMU ---

    // 1. Přidání nového cviku (z Pickeru)
    fun addExercise(exercise: Exercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()

        // Vytvoříme TemplateExercise s výchozími hodnotami
        val newTemplateItem = TemplateExercise(
            exercise = exercise,
            order = currentList.size + 1, // Jednoduché řazení na konec
            sets = "3",
            reps = "10",
            weight = null,
            rest = "60"
        )

        currentList.add(newTemplateItem)
        _templateExercises.value = currentList
    }

    // 2. Aktualizace existujícího cviku (když uživatel píše do EditTextu)
    fun updateTemplateExercise(updatedItem: TemplateExercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()

        // Najdeme index položky podle ID cviku (předpokládáme unikátní cviky v tréninku pro zjednodušení)
        val index = currentList.indexOfFirst { it.exercise.id == updatedItem.exercise.id }

        if (index != -1) {
            currentList[index] = updatedItem
            _templateExercises.value = currentList
        }
    }

    // 3. Smazání cviku ze seznamu
    fun deleteTemplateExercise(itemToDelete: TemplateExercise) {
        val currentList = _templateExercises.value.orEmpty().toMutableList()
        currentList.remove(itemToDelete)

        // Volitelné: Přečíslování pořadí (order)
        val reorderedList = currentList.mapIndexed { index, item ->
            item.copy(order = index + 1)
        }

        _templateExercises.value = reorderedList
    }

    fun saveTrainingUnit(name: String, note: String?, clientId: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {

            // 1. Vygenerujeme unikátní ID
            val newUnitId = java.util.UUID.randomUUID().toString()

            // 2. Vytvoříme objekt Tréninku
            val unitEntity = TrainingUnitEntity(
                id = newUnitId,
                name = name,
                note = note,
                clientId = clientId
            )

            // 3. Vytvoříme seznam entit pro vazební tabulku
            // OPRAVA: Používáme _templateExercises.value (ne addedExercises)
            val currentExercises = _templateExercises.value ?: emptyList()

            val exerciseEntities = currentExercises.map { templateItem ->
                TrainingUnitExerciseEntity(
                    trainingUnitId = newUnitId, // ID tréninku
                    exerciseId = templateItem.exercise.id, // ID cviku
                    orderIndex = templateItem.order, // Pořadí

                    // OPRAVA LOGIKY: Bereme hodnoty z editoru, ne natvrdo "4" a "10"
                    sets = templateItem.sets,
                    reps = templateItem.reps,
                    weight = templateItem.weight,
                    rest = templateItem.rest,

                    // Pokud máš v TemplateExercise i tato pole, napoj je stejně.
                    // Pokud ne, můžeš nechat null nebo defaulty, dokud je nepřidáš do UI.
                    time = null,
                    distance = null,
                    rir = null,

                    // Konfigurace (zatím natvrdo true, nebo si to přidej do TemplateExercise)
                    isRepsEnabled = true,
                    isWeightEnabled = true,
                    isTimeEnabled = false,
                    isDistanceEnabled = false,
                    isRirEnabled = false,
                    isRestEnabled = true
                )
            }

            // 4. Uložení
            // Ujisti se, že v Repository voláš dao.saveTrainingUnitWithExercises!
            repository.saveTrainingUnit(unitEntity, exerciseEntities)

            onSuccess()
        }
    }
}