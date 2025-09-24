package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para guardar una frase
 */
class SavePhraseUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(phrase: Phrase): Result<Unit> {
        return try {
            memoryRepository.savePhrase(phrase)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

