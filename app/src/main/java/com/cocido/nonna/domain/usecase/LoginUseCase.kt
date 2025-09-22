package com.cocido.nonna.domain.usecase

import android.util.Log
import com.cocido.nonna.domain.model.User
import com.cocido.nonna.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para el login de usuarios
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            Log.d("LoginUseCase", "Iniciando login para: $email")
            val result = authRepository.login(email, password)
            Log.d("LoginUseCase", "Login exitoso")
            result
        } catch (e: Exception) {
            Log.e("LoginUseCase", "Error en login: ${e.message}", e)
            Result.failure(e)
        }
    }
}


