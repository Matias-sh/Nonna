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
            Logger.d("Syncing vaults from remote for user: ${userId.value}")
            val vaultsResponse = vaultApiService.getUserVaults(userId.value)
            Logger.d("Received vaults response: count=${vaultsResponse.count}, results=${vaultsResponse.results.size}")
            
            val vaults = vaultsResponse.results.map { it.vaultDtoToDomain() }
            Logger.d("Converted ${vaults.size} vaults to domain models")
            
            // Guardar en cache local
            vaults.forEach { vault ->
                val entity = vault.vaultToEntity()
                vaultDao.insertVault(entity)
                Logger.d("Saved vault to local cache: ${vault.name} (${vault.id.value})")
            }
            
            Logger.d("Successfully synced ${vaults.size} vaults from remote")
            Result.success(vaults)
        } catch (e: Exception) {
            Logger.e("Failed to sync vaults from remote", throwable = e)
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
