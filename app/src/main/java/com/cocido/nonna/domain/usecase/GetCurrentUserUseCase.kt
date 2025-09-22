package com.cocido.nonna.domain.usecase

import com.cocido.nonna.data.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): com.cocido.nonna.data.remote.dto.UserDto? {
        return (authRepository as com.cocido.nonna.data.repository.AuthRepositoryImpl).getCurrentUser()
    }
}
