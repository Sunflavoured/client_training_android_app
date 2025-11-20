package com.example.client_training_app.data.database

import android.content.Context
import com.example.client_training_app.model.Client
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
}