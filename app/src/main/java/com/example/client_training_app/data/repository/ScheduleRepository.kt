package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.ScheduledWorkoutDao
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.database.ScheduledWorkoutDetail
import com.example.client_training_app.data.entity.ScheduledWorkoutEntity
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(context: Context) {

    private val dao: ScheduledWorkoutDao

    init {
        val database = DatabaseInstance.getDatabase(context)
        dao = database.scheduledWorkoutDao()
    }

    suspend fun scheduleWorkout(workout: ScheduledWorkoutEntity) {
        dao.insertScheduledWorkout(workout)
    }

    suspend fun deleteScheduledWorkout(id: Long) {
        dao.deleteScheduledWorkout(id)
    }

    // Pro zobrazení seznamu pod kalendářem
    fun getWorkoutsForDayFlow(clientId: String, date: Long): Flow<List<ScheduledWorkoutEntity>> {
        // Jednoduchý výpočet začátku a konce dne (předpokládáme, že 'date' je půlnoc)
        // V reálu je lepší používat Java Time API (LocalDate), ale pro Room stačí Long
        val startOfDay = date
        val endOfDay = date + 86400000 - 1 // + 24 hodin mínus 1 ms

        return dao.getWorkoutsForDay(clientId, startOfDay, endOfDay)
    }

    // Pro tečky v kalendáři
    fun getAllSchedulesFlow(clientId: String): Flow<List<ScheduledWorkoutEntity>> {
        return dao.getAllScheduledWorkouts(clientId)
    }

    fun getAllSchedulesWithDetailsFlow(clientId: String): Flow<List<ScheduledWorkoutDetail>> {
        return dao.getAllScheduledWorkoutsWithDetails(clientId)
    }

    fun getAllScheduledWorkoutsFlow(clientId: String): Flow<List<ScheduledWorkoutEntity>> {
        return dao.getAllScheduledWorkouts(clientId)
    }
    suspend fun markAsCompleted(id: Long) { dao.updateCompletionStatus(id, true) }

}