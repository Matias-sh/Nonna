package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject

class InviteMemberUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(vaultId: VaultId, email: String, role: String = "member"): Result<Unit> {
        return vaultRepository.inviteMember(vaultId, email, role)
    }
}
