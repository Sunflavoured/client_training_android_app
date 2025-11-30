package com.example.client_training_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.client_training_app.data.database.TrainingUnitWithExercises
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingUnitDao {

    // Vložení hlavičky (jednotky)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTrainingUnit(trainingUnit: TrainingUnitEntity)

    // Vložení cviku do jednotky
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertUnitExercise(exercise: TrainingUnitExerciseEntity)

    // 1. Seznam: Jednotky KONKRÉTNÍHO klienta
    @Query("SELECT * FROM training_units WHERE clientId = :clientId")
    fun getTrainingUnitsForClient(clientId: String): Flow<List<TrainingUnitEntity>>

    // 2. Seznam: GLOBÁLNÍ jednotky (kde clientId je NULL)
    @Query("SELECT * FROM training_units WHERE clientId IS NULL")
    fun getGlobalTrainingUnits(): Flow<List<TrainingUnitEntity>>

    // Načíst konkrétní jednotku KOMPLETNĚ i s cviky
    @Transaction
    @Query("SELECT * FROM training_units WHERE id = :unitId")
    suspend fun getTrainingUnitWithExercises(unitId: String): TrainingUnitWithExercises?
}