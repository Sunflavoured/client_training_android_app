package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.WorkoutSessionDao
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.data.entity.WorkoutSetResultEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(context: Context) {

    private val dao: WorkoutSessionDao

    init {
        val database = DatabaseInstance.getDatabase(context)
        dao = database.workoutSessionDao()
    }

    // Uložení celého tréninku (Hlavička + Série)
    suspend fun saveWorkout(session: WorkoutSessionEntity, sets: List<WorkoutSetResultEntity>) {
        dao.saveCompletedWorkout(session, sets)
    }

    // Získání historie tréninků klienta
    fun getClientHistoryFlow(clientId: String): Flow<List<WorkoutSessionEntity>> {
        return dao.getSessionsForClient(clientId)
    }

    // Získání historie jednoho cviku (pro grafy)
    fun getExerciseHistoryFlow(clientId: String, exerciseId: String): Flow<List<WorkoutSetResultEntity>> {
        return dao.getHistoryForExercise(clientId, exerciseId)
    }
}