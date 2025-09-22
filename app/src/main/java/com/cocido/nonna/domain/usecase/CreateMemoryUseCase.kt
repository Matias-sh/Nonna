package com.cocido.nonna.domain.usecase

import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para crear un nuevo recuerdo sensorial
 */
class CreateMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepositoryImpl
) {
    suspend operator fun invoke(memory: Memory): Result<MemoryId> {
        return try {
            memoryRepository.saveMemory(memory)
            Result.success(memory.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


