package com.cocido.nonna.domain.usecase

import android.util.Log
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.AuthResponse
import com.cocido.nonna.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para el login de usuarios
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        return try {
            Log.d("LoginUseCase", "Iniciando login para: $email")
            val request = LoginRequest(email, password)
            val response = authRepository.login(request)
            Log.d("LoginUseCase", "Login exitoso")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LoginUseCase", "Error en login: ${e.message}", e)
            Result.failure(e)
        }
    }
}


