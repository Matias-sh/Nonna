package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case para obtener el ID del usuario actual
 */
class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserId? {
        return authRepository.getCurrentUserId()
    }
}

