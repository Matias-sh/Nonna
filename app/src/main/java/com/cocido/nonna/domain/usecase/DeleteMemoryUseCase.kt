package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para eliminar un recuerdo
 */
class DeleteMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memoryId: MemoryId): Result<Unit> {
        return try {
            memoryRepository.deleteMemory(memoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
