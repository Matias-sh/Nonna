package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.User
import com.cocido.nonna.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para el registro de nuevos usuarios
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        return try {
            val result = authRepository.register(email, password, name)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


