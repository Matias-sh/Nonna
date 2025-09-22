package com.cocido.nonna.domain.repository

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.domain.model.PhraseId
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones con recuerdos y frases
 */
interface MemoryRepository {
    
    // Operaciones con recuerdos
    fun getMemoriesByVault(vaultId: VaultId): Flow<List<Memory>>
    suspend fun getMemoryById(memoryId: MemoryId): Memory?
    suspend fun saveMemory(memory: Memory)
    suspend fun deleteMemory(memoryId: MemoryId)
    suspend fun searchMemories(query: String, vaultId: VaultId): List<Memory>
    suspend fun getAllMemories(): List<Memory>
    
    // Operaciones con frases
    suspend fun getAllPhrases(): List<Phrase>
    suspend fun savePhrase(phrase: Phrase)
    suspend fun deletePhrase(phraseId: PhraseId)
}