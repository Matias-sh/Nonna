package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

/**
 * Use case para salir de un baúl
 */
class LeaveVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vaultId: VaultId, userId: UserId): Result<Unit> {
        return vaultRepository.leaveVault(vaultId, userId)
    }
}

