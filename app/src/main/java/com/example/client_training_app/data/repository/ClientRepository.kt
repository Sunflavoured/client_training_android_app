package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.ClientDao
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.data.entity.toClient
import com.example.client_training_app.data.entity.toEntity
import com.example.client_training_app.model.Client
import com.example.client_training_app.model.Measurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.client_training_app.model.toEntity
import com.example.client_training_app.model.toMeasurement

class ClientRepository(context: Context) {

    // Získáme instanci DAO
    private val clientDao: ClientDao = DatabaseInstance.getDatabase(context).clientDao()

    // 1. Získání všech klientů jako Flow
    fun getAllClientsFlow(): Flow<List<`Client`>> {
        // Mapujeme seznam entit (ClientEntity) na seznam modelů (Client)
        return clientDao.getAllClients().map { entities ->
            entities.map { it.toClient() }
        }
    }

    fun getClientByIdFlow(clientId: String): Flow<`Client`?> {
        // Použijeme .map ke konverzi Entity na aplikační model Client
        return clientDao.getClientByIdFlow(clientId)
            .map { entity -> entity?.toClient() }
    }

    // 2. Hledání klientů
    fun searchClientsFlow(query: String): Flow<List<`Client`>> {
        return clientDao.searchClients(query).map { entities ->
            entities.map { it.toClient() }
        }
    }

    // 3. Přidání nového klienta
    suspend fun addClient(client: `Client`) {
        clientDao.insert(client.toEntity())
    }

    // 4. Získání klienta podle ID (pro detail)
    suspend fun getClientById(clientId: String): `Client`? {
        return clientDao.getClientById(clientId)?.toClient()
    }

    // 5. Aktualizace/Smazání (volitelné, ale užitečné)
    suspend fun updateClient(client: Client) {
        clientDao.update(client.toEntity())
    }

    suspend fun deleteClient(client: Client) {
        clientDao.deleteClient(client.toEntity())
    }

    // --- METODY PRO MĚŘENÍ ---

    /** Uložení nového měření do databáze */
    suspend fun addMeasurement(measurement: Measurement) {
        // Převede model Measurement na databázovou entitu a uloží
        clientDao.insertMeasurement(measurement.toEntity())
    }

    /** Získání všech měření pro klienta */
    fun getMeasurementsForClientFlow(clientId: String): Flow<List<Measurement>> {
        // Získá data z databáze a mapuje (převádí) je na aplikační model Measurement
        return clientDao.getMeasurementsForClient(clientId).map { entities ->
            entities.map { it.toMeasurement() }
        }
    }
    /** Načtení tréninku podle clientId*/
    fun getTrainingSessionsFlow(clientId: String): Flow<List<WorkoutSessionEntity>> {
        return clientDao.getTrainingSessionsForClient(clientId)
    }
    /** Vložení tréninku */
    suspend fun addTrainingSession(session: WorkoutSessionEntity) {
        clientDao.insertTrainingSession(session)
    }
}