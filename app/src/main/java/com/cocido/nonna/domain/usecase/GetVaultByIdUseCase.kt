package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

/**
 * Use case para obtener un baúl por ID
 */
class GetVaultByIdUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vaultId: VaultId): Vault? {
        return vaultRepository.getVaultById(vaultId)
    }
}
