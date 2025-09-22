package com.cocido.nonna.domain.usecase

import com.cocido.nonna.data.repository.AuthRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, avatar: String? = null): Result<Unit> {
        return try {
            (authRepository as com.cocido.nonna.data.repository.AuthRepositoryImpl).updateUserProfile(name, avatar)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
