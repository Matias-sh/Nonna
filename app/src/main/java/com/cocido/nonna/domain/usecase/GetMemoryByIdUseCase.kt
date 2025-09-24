package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para obtener un recuerdo por ID
 */
class GetMemoryByIdUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memoryId: MemoryId): Result<Memory?> {
        return try {
            val memory = memoryRepository.getMemoryById(memoryId)
            Result.success(memory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

