package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case para obtener los baúles del usuario
 */
class GetVaultsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(userId: UserId): Result<List<com.cocido.nonna.domain.model.Vault>> {
        return try {
            val vaults = vaultRepository.getUserVaults(userId).first()
            Result.success(vaults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}