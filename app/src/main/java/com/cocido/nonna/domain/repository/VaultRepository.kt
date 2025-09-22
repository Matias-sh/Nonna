package com.cocido.nonna.domain.repository

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    suspend fun getVaults(): Result<List<Vault>>
    suspend fun getVaultById(vaultId: VaultId): Result<Vault>
    suspend fun createVault(name: String, description: String? = null): Result<Vault>
    suspend fun updateVault(vaultId: VaultId, name: String, description: String? = null): Result<Vault>
    suspend fun deleteVault(vaultId: VaultId): Result<Unit>
    suspend fun joinVault(vaultCode: String): Result<Vault>
    suspend fun inviteMember(vaultId: VaultId, email: String, role: String = "member"): Result<Unit>
    suspend fun removeMember(vaultId: VaultId, memberId: Int): Result<Unit>
    suspend fun updateMemberRole(vaultId: VaultId, memberId: Int, role: String): Result<Unit>
}
