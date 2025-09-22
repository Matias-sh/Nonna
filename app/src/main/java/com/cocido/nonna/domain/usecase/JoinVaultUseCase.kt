package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

class JoinVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vaultCode: String): Result<Vault> {
        return vaultRepository.joinVault(vaultCode)
    }
}
