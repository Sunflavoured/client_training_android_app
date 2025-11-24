package com.example.client_training_app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity)

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)

    @Query("SELECT * FROM clients ORDER BY lastName ASC, firstName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' ORDER BY lastName ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    /** Uloží nové měření. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    /** Získá všechna měření pro daného klienta, seřazená od nejnovějšího. */
    @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY date DESC")
    fun getMeasurementsForClient(clientId: String): Flow<List<MeasurementEntity>>
}