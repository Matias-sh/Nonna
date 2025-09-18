package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.dao.MemoryDao
import com.cocido.nonna.data.local.entity.toDomain
import com.cocido.nonna.data.local.entity.toEntity
import com.cocido.nonna.data.remote.api.MemoryApiService
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de recuerdos
 * Combina datos locales (Room) y remotos (API)
 */
@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao,
    private val memoryApiService: MemoryApiService
) {
    
    fun getMemoriesByVault(vaultId: VaultId): Flow<List<Memory>> {
        return memoryDao.getMemoriesByVault(vaultId.value).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getMemoryById(memoryId: MemoryId): Memory? {
        return memoryDao.getMemoryById(memoryId.value)?.toDomain()
    }
    
    suspend fun saveMemory(memory: Memory) {
        val entity = memory.toEntity()
        memoryDao.insertMemory(entity)
        
        // TODO: Sincronizar con API en background
        // syncWithRemote(memory)
    }
    
    suspend fun deleteMemory(memoryId: MemoryId) {
        memoryDao.deleteMemory(memoryId.value)
        
        // TODO: Sincronizar con API en background
        // memoryApiService.deleteMemory(memoryId.value)
    }
    
    suspend fun searchMemories(query: String, vaultId: VaultId): List<Memory> {
        return memoryDao.searchMemories(query, vaultId.value).map { it.toDomain() }
    }
    
    private suspend fun syncWithRemote(memory: Memory) {
        try {
            // TODO: Implementar sincronización con API
            // val dto = memory.toDto()
            // memoryApiService.createMemory(dto)
        } catch (e: Exception) {
            // TODO: Manejar error de sincronización
            // Log error or queue for retry
        }
    }
}

