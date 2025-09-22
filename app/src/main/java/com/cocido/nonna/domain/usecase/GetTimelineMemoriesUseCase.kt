package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para obtener recuerdos de la línea de tiempo
 */
class GetTimelineMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(year: Int? = null): Result<List<Memory>> {
        return try {
            val memories = memoryRepository.getAllMemories()
            val filteredMemories = if (year != null) {
                memories.filter { memory ->
                    memory.dateTaken?.let { timestamp ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = timestamp
                        calendar.get(java.util.Calendar.YEAR) == year
                    } ?: false
                }
            } else {
                memories
            }
            Result.success(filteredMemories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}