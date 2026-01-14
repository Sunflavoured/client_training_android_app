package com.example.client_training_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import kotlinx.coroutines.flow.Flow
import com.example.client_training_app.data.database.TrainingUnitWithExercises

@Dao
interface TrainingUnitDao {

    // --- INSERT / UPDATE ---

    // Vložení nebo aktualizace hlavičky (OnConflictStrategy.REPLACE to řeší za nás)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUnit(unit: TrainingUnitEntity)

    // Vložení seznamu cviků
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitExercises(exercises: List<TrainingUnitExerciseEntity>)

    // --- DELETE ---

    @Query("DELETE FROM training_unit_exercises WHERE trainingUnitId = :unitId")
    suspend fun deleteExercisesForUnit(unitId: String)

    @Query("DELETE FROM training_units WHERE id = :unitId")
    suspend fun deleteTrainingUnitById(unitId: String)

    // --- TRANSAKCE (Logika ukládání) ---

    // 1. Pro nový trénink (Insert)
    @Transaction
    suspend fun saveTrainingUnitWithExercises(
        unit: TrainingUnitEntity,
        exercises: List<TrainingUnitExerciseEntity>
    ) {
        insertOrUpdateUnit(unit)
        insertUnitExercises(exercises)
    }

    // 2. Pro editaci (Update = Smazat staré vazby + Vložit nové)
    @Transaction
    suspend fun updateTrainingUnitWithExercises(unit: TrainingUnitEntity, exercises: List<TrainingUnitExerciseEntity>) {
        // A) Aktualizuj hlavičku (změna názvu, poznámky)
        insertOrUpdateUnit(unit)

        // B) SMAŽ staré cviky (aby se nezdvojily)
        deleteExercisesForUnit(unit.id)

        // C) Vlož aktuální seznam cviků z editoru
        insertUnitExercises(exercises)
    }

    // --- SELECT (Flows & Data) ---

    @Query("SELECT * FROM training_units WHERE clientId = :clientId")
    fun getTrainingUnitsForClient(clientId: String): Flow<List<TrainingUnitEntity>>

    @Query("SELECT * FROM training_units")
    fun getAllTrainingUnits(): Flow<List<TrainingUnitEntity>>

    @Query("SELECT * FROM training_units WHERE clientId IS NULL")
    fun getGlobalTrainingUnits(): Flow<List<TrainingUnitEntity>>

    @Transaction
    @Query("SELECT * FROM training_units WHERE id = :unitId")
    suspend fun getTrainingUnitWithExercises(unitId: String): TrainingUnitWithExercises?

    @Query("SELECT * FROM training_units WHERE clientId IS NULL OR clientId = :clientId")
    fun getAvailableUnitsForClient(clientId: String): Flow<List<TrainingUnitEntity>>

}