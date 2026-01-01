package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.TrainingUnitDao
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.database.TrainingUnitWithExercises
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import kotlinx.coroutines.flow.Flow

class TrainingUnitRepository(context: Context) {

    private val trainingUnitDao: TrainingUnitDao

    init {
        val database = DatabaseInstance.getDatabase(context)
        trainingUnitDao = database.trainingUnitDao()
    }

    // --- INSERT ---

    suspend fun createTrainingUnit(unit: TrainingUnitEntity) {
        trainingUnitDao.insertTrainingUnit(unit)
    }

    suspend fun addExerciseToUnit(exercise: TrainingUnitExerciseEntity) {
        trainingUnitDao.insertUnitExercise(exercise)
    }

    // --- SELECT ---

    fun getClientUnitsFlow(clientId: String): Flow<List<TrainingUnitEntity>> {
        return trainingUnitDao.getTrainingUnitsForClient(clientId)
    }

    fun getGlobalUnitsFlow(): Flow<List<TrainingUnitEntity>> {
        return trainingUnitDao.getGlobalTrainingUnits()
    }

    suspend fun getTrainingUnitDetail(unitId: String): TrainingUnitWithExercises? {
        return trainingUnitDao.getTrainingUnitWithExercises(unitId)
    }
    //  metoda pro uložení kompletního tréninku
    suspend fun saveTrainingUnit(
        unit: TrainingUnitEntity,
        exercises: List<TrainingUnitExerciseEntity>
    ) {
        trainingUnitDao.saveTrainingUnitWithExercises(unit, exercises)
    }
}