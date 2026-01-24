package com.example.client_training_app.ui.exercise_history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.client_training_app.data.repository.WorkoutRepository
import com.example.client_training_app.model.ExerciseHistoryItem

class ExerciseHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(application)

    fun getHistory(clientId: String, exerciseId: String): LiveData<List<ExerciseHistoryItem>> {
        // Převedeme Flow z databáze na LiveData pro UI
        return repository.getExerciseHistory(clientId, exerciseId).asLiveData()
    }
}