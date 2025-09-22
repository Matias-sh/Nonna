package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

/**
 * Use case para eliminar un baúl
 */
class DeleteVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vaultId: VaultId): Result<Unit> {
        return vaultRepository.deleteVault(vaultId)
    }
}
