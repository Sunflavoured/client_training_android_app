package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.ScheduledWorkoutDao
import com.example.client_training_app.data.dao.TrainingUnitDao
import androidx.room.withTransaction
import com.example.client_training_app.data.database.AppDatabase
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.database.ScheduledWorkoutDetail
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import kotlinx.coroutines.flow.Flow
import com.example.client_training_app.data.database.TrainingUnitWithExercises

/** Kolikrát je šablona naplánovaná v kalendáři a kolikrát z toho už byla odcvičena. */
data class ScheduleUsage(val total: Int, val completed: Int)

class TrainingUnitRepository(context: Context) {

    private val database: AppDatabase = DatabaseInstance.getDatabase(context)
    private val trainingUnitDao: TrainingUnitDao = database.trainingUnitDao()
    private val scheduledWorkoutDao: ScheduledWorkoutDao = database.scheduledWorkoutDao()

    // --- INSERT / CREATE ---

    suspend fun saveTrainingUnit(
        unit: TrainingUnitEntity,
        exercises: List<TrainingUnitExerciseEntity>
    ) {
        trainingUnitDao.saveTrainingUnitWithExercises(unit, exercises)
    }

    // --- UPDATE (Tato metoda chyběla) ---
    suspend fun updateTrainingUnit(
        unit: TrainingUnitEntity,
        exercises: List<TrainingUnitExerciseEntity>
    ) {
        // Voláme opravenou transakci v DAO
        trainingUnitDao.updateTrainingUnitWithExercises(unit, exercises)
    }

    // --- READ / SELECT ---

    fun getClientUnitsFlow(clientId: String): Flow<List<TrainingUnitEntity>> {
        return trainingUnitDao.getTrainingUnitsForClient(clientId)
    }

    fun getGlobalUnitsFlow(): Flow<List<TrainingUnitEntity>> {
        return trainingUnitDao.getGlobalTrainingUnits()
    }
    fun getAllUnitsFlow(): Flow<List<TrainingUnitEntity>> {
        return trainingUnitDao.getAllTrainingUnits()
    }


    suspend fun getTrainingUnitWithExercises(unitId: String): TrainingUnitWithExercises? {
        return trainingUnitDao.getTrainingUnitWithExercises(unitId)
    }

    fun getAvailableUnitsForClientFlow(clientId: String): Flow<List<TrainingUnitEntity>> {
        return trainingUnitDao.getAvailableUnitsForClient(clientId)
    }



    // --- DELETE ---

    suspend fun getScheduleUsage(unitId: String): ScheduleUsage {
        return ScheduleUsage(
            total = scheduledWorkoutDao.countForUnit(unitId),
            completed = scheduledWorkoutDao.countCompletedForUnit(unitId)
        )
    }

    /**
     * Smaže šablonu i její záznamy v kalendáři v jedné transakci. Plán v kalendáři odkazuje na šablonu
     * cizím klíčem bez akce, takže šablonu s naplánovaným tréninkem jinak nejde smazat.
     * Výsledky odcvičených tréninků (historie) zůstávají.
     */
    suspend fun deleteTrainingUnit(unitId: String) {
        database.withTransaction {
            scheduledWorkoutDao.deleteByUnit(unitId)
            trainingUnitDao.deleteTrainingUnitById(unitId)
        }
    }
}