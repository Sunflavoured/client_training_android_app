package com.example.client_training_app.data.database

import android.content.Context
import com.example.client_training_app.model.Client
import com.example.client_training_app.model.Measurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClientRepository(context: Context) {

    // Získáme instanci DAO
    private val clientDao: ClientDao = DatabaseInstance.getDatabase(context).clientDao()

    // 1. Získání všech klientů jako Flow
    fun getAllClientsFlow(): Flow<List<Client>> {
        // Mapujeme seznam entit (ClientEntity) na seznam modelů (Client)
        return clientDao.getAllClients().map { entities ->
            entities.map { it.toClient() }
        }
    }

    // 2. Hledání klientů
    fun searchClientsFlow(query: String): Flow<List<Client>> {
        return clientDao.searchClients(query).map { entities ->
            entities.map { it.toClient() }
        }
    }

    // 3. Přidání nového klienta
    suspend fun addClient(client: Client) {
        clientDao.insert(client.toEntity())
    }

    // 4. Získání klienta podle ID (pro detail)
    suspend fun getClientById(clientId: String): Client? {
        return clientDao.getClientById(clientId)?.toClient()
    }

    // 5. Aktualizace/Smazání (volitelné, ale užitečné)
    suspend fun updateClient(client: Client) {
        clientDao.update(client.toEntity())
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
}