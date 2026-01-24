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
    fun getExerciseHistory(clientId: String, exerciseId: String) =
        dao.getExerciseHistory(clientId, exerciseId)

    // 1. Zjistit, jestli už k tomuto plánu (z kalendáře) existuje rozdělaný/hotový trénink
    suspend fun getSessionByScheduleId(scheduleId: Long): WorkoutSessionEntity? {
        return dao.getSessionByScheduleId(scheduleId)
    }

    // 2. Načíst série pro konkrétní trénink (abychom mohli předvyplnit data při editaci)
    suspend fun getSetsForSession(sessionId: String): List<WorkoutSetResultEntity> {
        return dao.getSetsForSession(sessionId)
    }
}