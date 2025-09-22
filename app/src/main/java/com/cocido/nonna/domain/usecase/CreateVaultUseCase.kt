package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

/**
 * Use case para crear un nuevo baúl
 */
class CreateVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vault: Vault): Result<VaultId> {
        return vaultRepository.createVault(vault)
    }
}