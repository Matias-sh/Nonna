package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

/**
 * Use case para actualizar un baúl existente
 */
class UpdateVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vault: Vault): Result<Unit> {
        return vaultRepository.updateVault(vault)
    }
}

