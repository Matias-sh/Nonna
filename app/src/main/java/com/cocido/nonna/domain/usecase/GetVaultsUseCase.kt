package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

class GetVaultsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(): Result<List<Vault>> {
        return vaultRepository.getVaults()
    }
}
