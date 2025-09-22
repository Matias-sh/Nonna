package com.cocido.nonna.domain.repository

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones con baúles
 */
interface VaultRepository {
    
    fun getUserVaults(userId: UserId): Flow<List<Vault>>
    suspend fun getVaultById(vaultId: VaultId): Vault?
    suspend fun createVault(vault: Vault): Result<VaultId>
    suspend fun updateVault(vault: Vault): Result<Unit>
    suspend fun deleteVault(vaultId: VaultId): Result<Unit>
    suspend fun joinVault(vaultId: VaultId, userId: UserId): Result<Unit>
    suspend fun leaveVault(vaultId: VaultId, userId: UserId): Result<Unit>
}