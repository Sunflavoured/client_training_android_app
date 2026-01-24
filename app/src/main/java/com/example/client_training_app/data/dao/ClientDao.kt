package com.example.client_training_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.client_training_app.data.entity.ClientEntity
import com.example.client_training_app.data.entity.MeasurementEntity
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(client: ClientEntity)

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("SELECT * FROM clients ORDER BY lastName ASC, firstName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE id = :clientId")
    fun getClientByIdFlow(clientId: String): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' ORDER BY lastName ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    /** Uloží nové měření. */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    /** Získá všechna měření pro daného klienta, seřazená od nejnovějšího. */
    @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY date DESC")
    fun getMeasurementsForClient(clientId: String): Flow<List<MeasurementEntity>>

    /** Vložení tréninku */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTrainingSession(session: WorkoutSessionEntity)

    // Načte tréninky pro konkrétního klienta
    @Query("SELECT * FROM workout_sessions WHERE clientId = :clientId")
    fun getTrainingSessionsForClient(clientId: String): Flow<List<WorkoutSessionEntity>>
}