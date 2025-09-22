package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import javax.inject.Inject

/**
 * Caso de uso para obtener recuerdos ordenados cronológicamente para la línea de tiempo
 */
class GetTimelineMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepositoryImpl
) {
    suspend operator fun invoke(yearFilter: Int? = null): Result<List<Memory>> {
        return try {
            val memories = memoryRepository.getAllMemories()
            
            // Filtrar por año si se especifica
            val filteredMemories = if (yearFilter != null) {
                memories.filter { memory ->
                    memory.dateTaken?.let { timestamp ->
                        try {
                            val year = java.time.Instant.ofEpochMilli(timestamp)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                                .year
                            year == yearFilter
                        } catch (e: Exception) {
                            false
                        }
                    } ?: false
                }
            } else {
                memories
            }
            
            // Ordenar por fecha (más recientes primero)
            val sortedMemories = filteredMemories.sortedByDescending { memory ->
                memory.dateTaken ?: 0L
            }
            
            Result.success(sortedMemories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


