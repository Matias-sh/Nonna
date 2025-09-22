package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las frases
 */
class GetPhrasesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepositoryImpl
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


