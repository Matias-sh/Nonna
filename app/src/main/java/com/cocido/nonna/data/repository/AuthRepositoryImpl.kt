package com.cocido.nonna.data.repository

import com.cocido.nonna.domain.model.User
import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de autenticación
 * Por ahora es temporal hasta implementar la autenticación real
 */
@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    
    // Usuario temporal para desarrollo
    private val tempUser = User(
        id = UserId("user_1"),
        email = "usuario@nonna.com",
        name = "Usuario Demo",
        profileImageUrl = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    override suspend fun login(email: String, password: String): Result<User> {
        // TODO: Implementar login real con API
        return Result.success(tempUser)
    }
    
    override suspend fun register(email: String, password: String, name: String): Result<User> {
        // TODO: Implementar registro real con API
        return Result.success(tempUser)
    }
    
    override suspend fun logout(): Result<Unit> {
        // TODO: Implementar logout real
        return Result.success(Unit)
    }
    
    override fun getCurrentUser(): Flow<User?> {
        // TODO: Implementar obtención del usuario actual
        return flowOf(tempUser)
    }
    
    override suspend fun getCurrentUserId(): UserId? {
        // TODO: Implementar obtención del ID del usuario actual
        return tempUser.id
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        // TODO: Implementar refresh de token
        return Result.success(Unit)
    }
    
    override suspend fun isAuthenticated(): Boolean {
        // TODO: Implementar verificación de autenticación
        return true
    }
}