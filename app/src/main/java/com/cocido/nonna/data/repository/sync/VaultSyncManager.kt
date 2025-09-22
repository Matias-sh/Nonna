package com.cocido.nonna.data.repository.sync

import com.cocido.nonna.data.local.dao.VaultDao
import com.cocido.nonna.data.local.entity.toDomain as vaultEntityToDomain
import com.cocido.nonna.data.local.entity.toEntity as vaultToEntity
import com.cocido.nonna.data.remote.api.VaultApiService
import com.cocido.nonna.data.remote.dto.toDomain as vaultDtoToDomain
import com.cocido.nonna.data.remote.dto.toRequest
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager responsable de la sincronización de baúles entre local y remoto
 * Sigue el principio de responsabilidad única (SRP)
 */
@Singleton
class VaultSyncManager @Inject constructor(
    private val vaultDao: VaultDao,
    private val vaultApiService: VaultApiService
) {
    
    /**
     * Sincroniza baúles desde la API y los guarda localmente
     */
    suspend fun syncVaultsFromRemote(userId: UserId): Result<List<Vault>> {
        return try {
            val vaultsDto = vaultApiService.getUserVaults(userId.value)
            val vaults = vaultsDto.map { it.vaultDtoToDomain() }
            
            // Guardar en cache local
            vaults.forEach { vault ->
                val entity = vault.vaultToEntity()
                vaultDao.insertVault(entity)
            }
            
            Result.success(vaults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza un baúl creado localmente con la API
     */
    suspend fun syncVaultToRemote(vault: Vault): Result<Vault> {
        return try {
            val request = vault.toRequest()
            val response = vaultApiService.createVault(request)
            val syncedVault = response.vaultDtoToDomain()
            
            // Actualizar cache local con datos del servidor
            val updatedEntity = syncedVault.vaultToEntity()
            vaultDao.insertVault(updatedEntity)
            
            Result.success(syncedVault)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza actualización de baúl con la API
     */
    suspend fun syncVaultUpdateToRemote(vault: Vault): Result<Unit> {
        return try {
            val request = vault.toRequest()
            vaultApiService.updateVault(vault.id.value, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza eliminación de baúl con la API
     */
    suspend fun syncVaultDeletionToRemote(vaultId: VaultId): Result<Unit> {
        return try {
            vaultApiService.deleteVault(vaultId.value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
