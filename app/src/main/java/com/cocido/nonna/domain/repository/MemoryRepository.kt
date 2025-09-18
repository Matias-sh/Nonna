package com.cocido.nonna.domain.repository

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones de recuerdos
 */
interface MemoryRepository {
    
    /**
     * Obtiene todos los recuerdos de una bóveda
     */
    fun getMemoriesByVault(vaultId: VaultId): Flow<List<Memory>>
    
    /**
     * Obtiene un recuerdo por ID
     */
    suspend fun getMemoryById(memoryId: MemoryId): Memory?
    
    /**
     * Crea un nuevo recuerdo
     */
    suspend fun createMemory(memory: Memory): MemoryId
    
    /**
     * Actualiza un recuerdo existente
     */
    suspend fun updateMemory(memory: Memory)
    
    /**
     * Elimina un recuerdo
     */
    suspend fun deleteMemory(memoryId: MemoryId)
    
    /**
     * Busca recuerdos por texto
     */
    suspend fun searchMemories(query: String, vaultId: VaultId): List<Memory>
}
