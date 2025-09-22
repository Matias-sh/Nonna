package com.cocido.nonna.domain.repository

import com.cocido.nonna.domain.model.User
import com.cocido.nonna.domain.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones de autenticación
 */
interface AuthRepository {
    
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    suspend fun getCurrentUserId(): UserId?
    suspend fun refreshToken(): Result<Unit>
    suspend fun isAuthenticated(): Boolean
}
