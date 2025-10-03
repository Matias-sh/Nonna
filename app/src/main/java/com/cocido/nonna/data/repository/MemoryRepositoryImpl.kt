package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.dao.MemoryDao
import com.cocido.nonna.data.local.dao.PhraseDao
import com.cocido.nonna.data.local.entity.toDomain
import com.cocido.nonna.data.local.entity.toEntity
import com.cocido.nonna.data.remote.api.MemoryApiService
import com.cocido.nonna.data.remote.dto.toDomain as dtoToDomain
import com.cocido.nonna.data.remote.dto.dtoToEntity
import com.cocido.nonna.data.remote.dto.toRequest
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.domain.model.PhraseId
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.MemoryRepository
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
    private val phraseDao: PhraseDao,
    private val memoryApiService: MemoryApiService
) : MemoryRepository {
    
    override fun getMemoriesByVault(vaultId: VaultId): Flow<List<Memory>> {
        return kotlinx.coroutines.flow.flow {
            try {
                // Obtener desde la API
                val memoriesDto = memoryApiService.getMemories(vault = vaultId.value)
                val memories = memoriesDto.map { it.dtoToDomain() }

                // Guardar en cache local
                memories.forEach { memory ->
                    val entity = memory.toEntity()
                    memoryDao.insertMemory(entity)
                }

                emit(memories)
            } catch (e: Exception) {
                // Si falla la API, intentar obtener desde cache local
                memoryDao.getMemoriesByVault(vaultId.value).collect { entities ->
                    val memories = entities.map { it.toDomain() }
                    emit(memories)
                }
            }
        }
    }
    
    override suspend fun getMemoryById(memoryId: MemoryId): Memory? {
        return memoryDao.getMemoryById(memoryId.value)?.toDomain()
    }
    
    override suspend fun saveMemory(memory: Memory) {
        val entity = memory.toEntity()
        memoryDao.insertMemory(entity)
        
        // Sincronizar con API en background
        try {
            val request = memory.toRequest()
            val response = memoryApiService.createMemory(request)
            // Actualizar con la respuesta del servidor
            val updatedEntity = response.dtoToEntity()
            memoryDao.insertMemory(updatedEntity)
        } catch (e: Exception) {
            // TODO: Manejar error de sincronización
            // Log error or queue for retry
        }
    }
    
    override suspend fun deleteMemory(memoryId: MemoryId) {
        val entity = memoryDao.getMemoryById(memoryId.value)
        if (entity != null) {
            memoryDao.deleteMemory(entity)
        }
        
        // TODO: Sincronizar con API en background
        // memoryApiService.deleteMemory(memoryId.value)
    }
    
    override suspend fun searchMemories(query: String, vaultId: VaultId): List<Memory> {
        return emptyList() // TODO: Implementar cuando esté disponible
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
    
    // Métodos para frases
    override suspend fun getAllPhrases(): List<Phrase> {
        return emptyList() // TODO: Implementar cuando esté disponible
    }
    
    override suspend fun savePhrase(phrase: Phrase) {
        // TODO: Implementar cuando esté disponible
    }
    
    override suspend fun deletePhrase(phraseId: PhraseId) {
        // TODO: Implementar cuando esté disponible
    }
    
    // Método temporal para obtener todos los recuerdos (usado por TimelineFragment)
    override suspend fun getAllMemories(): List<Memory> {
        return emptyList() // TODO: Implementar cuando esté disponible
    }

}

