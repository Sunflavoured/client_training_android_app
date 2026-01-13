package com.example.client_training_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.client_training_app.data.database.ScheduledWorkoutDetail
import com.example.client_training_app.data.entity.ScheduledWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledWorkoutDao {

    // Naplánování tréninku
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledWorkout(workout: ScheduledWorkoutEntity)

    // Smazání z kalendáře
    @Query("DELETE FROM scheduled_workouts WHERE id = :id")
    suspend fun deleteScheduledWorkout(id: Long)

    // 1. Získat tréninky pro konkrétního klienta a konkrétní DEN
    // Používáme rozsah 'od-do' (start dne a konec dne), abychom chytili vše
    @Query("""
        SELECT * FROM scheduled_workouts 
        WHERE clientId = :clientId 
        AND date >= :startOfDay AND date <= :endOfDay
        ORDER BY date ASC
    """)
    fun getWorkoutsForDay(clientId: String, startOfDay: Long, endOfDay: Long): Flow<List<ScheduledWorkoutEntity>>

    // 2. Získat všechny naplánované tréninky pro klienta (např. pro vykreslení teček v kalendáři v celém měsíci)
    // Stačí nám jen data, abychom věděli, kde udělat tečku
    @Query("SELECT * FROM scheduled_workouts WHERE clientId = :clientId")
    fun getAllScheduledWorkouts(clientId: String): Flow<List<ScheduledWorkoutEntity>>

    // Označit jako splněno (to se bude hodit v budoucnu)
    @Query("UPDATE scheduled_workouts SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    // V ScheduledWorkoutDao.kt
    @Transaction // Nutné pro @Relation
    @Query("SELECT * FROM scheduled_workouts WHERE clientId = :clientId")
    fun getAllScheduledWorkoutsWithDetails(clientId: String): Flow<List<ScheduledWorkoutDetail>>
}