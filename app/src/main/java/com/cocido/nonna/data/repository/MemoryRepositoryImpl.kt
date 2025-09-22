package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.dao.MemoryDao
import com.cocido.nonna.data.local.dao.PhraseDao
import com.cocido.nonna.data.local.entity.toDomain as entityToDomain
import com.cocido.nonna.data.local.entity.toEntity as memoryToEntity
import com.cocido.nonna.data.remote.api.MemoryApiService
import com.cocido.nonna.data.remote.dto.toDomain as dtoToDomain
import com.cocido.nonna.data.remote.dto.toEntity as dtoToEntity
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
                // Intentar obtener desde la API primero
                val memoriesDto = memoryApiService.getMemories(vault = vaultId.value)
                val memories = memoriesDto.map { it.dtoToDomain() }

                // Guardar en cache local
                memories.forEach { memory ->
                    val entity = memory.memoryToEntity()
                    memoryDao.insertMemory(entity)
                }

                emit(memories)
            } catch (e: Exception) {
                // Si falla la API, intentar obtener desde cache local
                memoryDao.getMemoriesByVault(vaultId.value).collect { entities ->
                    val memories = entities.map { it.entityToDomain() }

                    if (memories.isEmpty()) {
                        emit(createDummyMemories(vaultId))
                    } else {
                        emit(memories)
                    }
                }
            }
        }
    }
    
    override suspend fun getMemoryById(memoryId: MemoryId): Memory? {
        return memoryDao.getMemoryById(memoryId.value)?.entityToDomain()
    }
    
    override suspend fun saveMemory(memory: Memory) {
        val entity = memory.memoryToEntity()
        memoryDao.insertMemory(entity)
        
        // TODO: Sincronizar con API en background
        // syncWithRemote(memory)
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

    private fun createDummyMemories(vaultId: VaultId): List<Memory> {
        return listOf(
            Memory(
                id = MemoryId("memory_1"),
                vaultId = vaultId,
                title = "Abuela cocinando",
                type = com.cocido.nonna.domain.model.MemoryType.PHOTO_WITH_AUDIO,
                photoLocalPath = null,
                photoRemoteUrl = "https://picsum.photos/400/300?random=1",
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(com.cocido.nonna.domain.model.PersonId("person_1")),
                tags = listOf("cocina", "familia", "abuela"),
                dateTaken = System.currentTimeMillis() - 86400000L * 30, // 30 días atrás
                location = "Casa de la abuela",
                createdAt = System.currentTimeMillis() - 86400000L * 30,
                updatedAt = System.currentTimeMillis() - 86400000L * 30
            ),
            Memory(
                id = MemoryId("memory_2"),
                vaultId = vaultId,
                title = "Receta del pastel de manzana",
                type = com.cocido.nonna.domain.model.MemoryType.RECIPE,
                photoLocalPath = null,
                photoRemoteUrl = "https://picsum.photos/400/300?random=2",
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = true,
                transcript = "Ingredientes: 3 manzanas, 2 tazas de harina, 1 taza de azúcar...",
                people = listOf(com.cocido.nonna.domain.model.PersonId("person_1"), com.cocido.nonna.domain.model.PersonId("person_2")),
                tags = listOf("receta", "postre", "manzana"),
                dateTaken = System.currentTimeMillis() - 86400000L * 15, // 15 días atrás
                location = "Cocina familiar",
                createdAt = System.currentTimeMillis() - 86400000L * 15,
                updatedAt = System.currentTimeMillis() - 86400000L * 15
            ),
            Memory(
                id = MemoryId("memory_3"),
                vaultId = vaultId,
                title = "Historia de la boda",
                type = com.cocido.nonna.domain.model.MemoryType.AUDIO_ONLY,
                photoLocalPath = null,
                photoRemoteUrl = null,
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(com.cocido.nonna.domain.model.PersonId("person_1")),
                tags = listOf("historia", "boda", "familia"),
                dateTaken = System.currentTimeMillis() - 86400000L * 60, // 60 días atrás
                location = "Salón familiar",
                createdAt = System.currentTimeMillis() - 86400000L * 60,
                updatedAt = System.currentTimeMillis() - 86400000L * 60
            )
        )
    }
}

