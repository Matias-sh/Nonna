package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.MemoryId

/**
 * Use case para adjuntar audio a un recuerdo existente
 */
interface AttachAudioToMemoryUseCase {
    suspend operator fun invoke(memoryId: MemoryId, audioPath: String): Result<Unit>
}

