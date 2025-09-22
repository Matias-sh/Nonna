package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para obtener todas las frases
 */
class GetPhrasesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(): Result<List<Phrase>> {
        return try {
            val phrases = memoryRepository.getAllPhrases()
            Result.success(phrases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}