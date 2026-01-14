package com.example.client_training_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.data.entity.WorkoutSetResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    // 1. Vložení hlavičky tréninku
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    // 2. Vložení seznamu sérií
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetResultEntity>)

    // --- TRANSAKCE: Uloží kompletní trénink najednou ---
    @Transaction
    suspend fun saveCompletedWorkout(session: WorkoutSessionEntity, sets: List<WorkoutSetResultEntity>) {
        insertSession(session)
        insertSets(sets)
    }

    // 3. Načtení historie tréninků pro klienta (Seřazeno od nejnovějšího)
    @Query("SELECT * FROM workout_sessions WHERE clientId = :clientId ORDER BY startTime DESC")
    fun getSessionsForClient(clientId: String): Flow<List<WorkoutSessionEntity>>

    // 4. Načtení detailů konkrétního tréninku (série)
    @Query("SELECT * FROM workout_set_results WHERE sessionId = :sessionId ORDER BY exerciseId, setNumber")
    suspend fun getSetsForSession(sessionId: String): List<WorkoutSetResultEntity>

    // 5. (Pro budoucí grafy) Načíst historii konkrétního cviku pro klienta
    // Spojujeme tabulky, abychom mohli filtrovat podle klienta i cviku
    @Query("""
        SELECT sets.* FROM workout_set_results AS sets
        INNER JOIN workout_sessions AS session ON sets.sessionId = session.id
        WHERE session.clientId = :clientId AND sets.exerciseId = :exerciseId
        ORDER BY session.startTime DESC
    """)
    fun getHistoryForExercise(clientId: String, exerciseId: String): Flow<List<WorkoutSetResultEntity>>

    // načtení plánovaého tréninku podle ID, aby měl předvyplněná data
    @Query("SELECT * FROM workout_sessions WHERE scheduledWorkoutId = :scheduleId LIMIT 1")
    suspend fun getSessionByScheduleId(scheduleId: Long): WorkoutSessionEntity?
}