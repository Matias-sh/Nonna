package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

class CreateVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Result<Vault> {
        return vaultRepository.createVault(name, description)
    }
}
