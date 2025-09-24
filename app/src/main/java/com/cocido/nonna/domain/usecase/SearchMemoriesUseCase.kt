package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para buscar recuerdos
 */
class SearchMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(query: String, vaultId: VaultId): Result<List<Memory>> {
        return try {
            val memories = memoryRepository.searchMemories(query, vaultId)
            Result.success(memories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

