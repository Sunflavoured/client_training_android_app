package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.TrainingUnitDao
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import kotlinx.coroutines.flow.Flow
import com.example.client_training_app.data.database.TrainingUnitWithExercises

class TrainingUnitRepository(context: Context) {

    private val trainingUnitDao: TrainingUnitDao

    init {
        val database = DatabaseInstance.getDatabase(context)
        trainingUnitDao = database.trainingUnitDao()
    }

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

    suspend fun getTrainingUnitWithExercises(unitId: String): TrainingUnitWithExercises? {
        return trainingUnitDao.getTrainingUnitWithExercises(unitId)
    }

    // --- DELETE ---

    suspend fun deleteTrainingUnit(unitId: String) {
        trainingUnitDao.deleteTrainingUnitById(unitId)
    }
}