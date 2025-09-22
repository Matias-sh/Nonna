package com.cocido.nonna.domain.usecase

import com.cocido.nonna.data.remote.dto.RegisterRequest
import com.cocido.nonna.data.remote.dto.AuthResponse
import com.cocido.nonna.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para el registro de nuevos usuarios
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(email, password, name)
            val response = authRepository.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


