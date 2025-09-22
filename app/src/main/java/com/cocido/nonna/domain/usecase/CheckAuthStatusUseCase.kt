package com.cocido.nonna.domain.usecase

import com.cocido.nonna.data.repository.AuthRepository
import javax.inject.Inject

class CheckAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return (authRepository as com.cocido.nonna.data.repository.AuthRepositoryImpl).checkAndRefreshToken()
    }
}
