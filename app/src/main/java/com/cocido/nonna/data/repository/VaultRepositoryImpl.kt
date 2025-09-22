package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.api.VaultApiService
import com.cocido.nonna.data.remote.dto.toDomain
import com.cocido.nonna.data.remote.dto.CreateVaultRequest
import com.cocido.nonna.data.remote.dto.JoinVaultRequest
import com.cocido.nonna.data.remote.dto.InviteMemberRequest
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.VaultRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(
    private val vaultApiService: VaultApiService
) : VaultRepository {
    
    override suspend fun getVaults(): Result<List<Vault>> {
        return try {
            val vaultDtos = vaultApiService.getVaults()
            val vaults = vaultDtos.map { it.toDomain() }
            Result.success(vaults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVaultById(vaultId: VaultId): Result<Vault> {
        return try {
            val vaultDto = vaultApiService.getVaultById(vaultId.value)
            Result.success(vaultDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createVault(name: String, description: String?): Result<Vault> {
        return try {
            val request = CreateVaultRequest(name, description)
            val vaultDto = vaultApiService.createVault(request)
            Result.success(vaultDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateVault(vaultId: VaultId, name: String, description: String?): Result<Vault> {
        return try {
            val request = CreateVaultRequest(name, description)
            val vaultDto = vaultApiService.updateVault(vaultId.value, request)
            Result.success(vaultDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteVault(vaultId: VaultId): Result<Unit> {
        return try {
            vaultApiService.deleteVault(vaultId.value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun joinVault(vaultCode: String): Result<Vault> {
        return try {
            val request = JoinVaultRequest(vaultCode)
            val vaultDto = vaultApiService.joinVault(request)
            Result.success(vaultDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun inviteMember(vaultId: VaultId, email: String, role: String): Result<Unit> {
        return try {
            val request = InviteMemberRequest(email, role)
            vaultApiService.inviteMember(vaultId.value, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeMember(vaultId: VaultId, memberId: Int): Result<Unit> {
        return try {
            vaultApiService.removeMember(vaultId.value, memberId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMemberRole(vaultId: VaultId, memberId: Int, role: String): Result<Unit> {
        return try {
            val request = mapOf("role" to role)
            vaultApiService.updateMemberRole(vaultId.value, memberId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
