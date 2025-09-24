package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.PhraseId
import com.cocido.nonna.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * Use case para eliminar una frase
 */
class DeletePhraseUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(phraseId: PhraseId): Result<Unit> {
        return try {
            memoryRepository.deletePhrase(phraseId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

