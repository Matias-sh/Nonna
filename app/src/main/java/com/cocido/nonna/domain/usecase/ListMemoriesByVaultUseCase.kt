package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para listar recuerdos por baúl
 */
class ListMemoriesByVaultUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(vaultId: VaultId): Flow<List<Memory>> {
        return memoryRepository.getMemoriesByVault(vaultId)
    }
}