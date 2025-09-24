package com.cocido.nonna.data.repository

import com.cocido.nonna.core.logging.Logger
import com.cocido.nonna.core.network.NetworkManager
import com.cocido.nonna.data.local.dao.VaultDao
import com.cocido.nonna.data.local.entity.toDomain as vaultEntityToDomain
import com.cocido.nonna.data.local.entity.toEntity as vaultToEntity
import com.cocido.nonna.data.repository.sync.VaultSyncManager
import com.cocido.nonna.domain.error.RepositoryError
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de baúles
 * Combina datos locales (Room) y remotos (API)
 */
@Singleton
class VaultRepositoryImpl @Inject constructor(
    private val vaultDao: VaultDao,
    private val vaultSyncManager: VaultSyncManager,
    private val networkManager: NetworkManager
) : VaultRepository {
    
    override fun getUserVaults(userId: UserId): Flow<List<Vault>> {
        return try {
            Logger.d("Getting user vaults for userId: ${userId.value}")
            
            // Verificar conectividad de red
            val isConnected = networkManager.isCurrentlyConnected()
            
                if (isConnected) {
                    Logger.d("Network connected, attempting to sync vaults from remote")
                    // Intentar sincronizar desde la API
                    val syncResult = vaultSyncManager.syncVaultsFromRemote(userId)
                    if (syncResult.isSuccess) {
                        val vaults = syncResult.getOrNull() ?: emptyList()
                        Logger.d("Successfully synced ${vaults.size} vaults from remote")
                        if (vaults.isEmpty()) {
                            Logger.d("No remote vaults found, creating default vault")
                            flowOf(createDefaultVault(userId))
                        } else {
                            flowOf(vaults)
                        }
                    } else {
                        Logger.w("Failed to sync vaults from remote: ${syncResult.exceptionOrNull()?.message}")
                        // Fallback a cache local
                        vaultDao.getUserVaults(userId.value).map { entities ->
                            val vaults = entities.map { it.vaultEntityToDomain() }
                            if (vaults.isEmpty()) {
                                Logger.d("No local vaults found, creating default vault")
                                createDefaultVault(userId)
                            } else {
                                Logger.d("Found ${vaults.size} vaults in local cache")
                                vaults
                            }
                        }
                    }
                } else {
                    Logger.w("No network connection, using local cache")
                    // Fallback a cache local
                    vaultDao.getUserVaults(userId.value).map { entities ->
                        val vaults = entities.map { it.vaultEntityToDomain() }
                        if (vaults.isEmpty()) {
                            Logger.d("No local vaults found, creating default vault")
                            createDefaultVault(userId)
                        } else {
                            Logger.d("Found ${vaults.size} vaults in local cache")
                            vaults
                        }
                    }
                }
        } catch (e: Exception) {
            Logger.e("Error getting user vaults", throwable = e)
            flowOf(createDefaultVault(userId))
        }
    }
    
    override suspend fun getVaultById(vaultId: VaultId): Vault? {
        return try {
            Logger.d("Getting vault by id: ${vaultId.value}")
            vaultDao.getVaultById(vaultId.value)?.vaultEntityToDomain()
        } catch (e: Exception) {
            Logger.e("Error getting vault by id: ${vaultId.value}", throwable = e)
            null
        }
    }
    
    override suspend fun createVault(vault: Vault): Result<VaultId> {
        return try {
            Logger.d("Creating vault: ${vault.name}")
            
            // Validar datos de entrada
            if (vault.name.isBlank()) {
                return Result.failure(RepositoryError.ValidationError("El nombre del baúl no puede estar vacío"))
            }
            
            // Verificar conectividad para sincronización
            val isConnected = networkManager.isCurrentlyConnected()
            
            if (isConnected) {
                // Sincronizar con API
                val syncResult = vaultSyncManager.syncVaultToRemote(vault)
                if (syncResult.isSuccess) {
                    val syncedVault = syncResult.getOrNull()
                    Logger.d("Successfully created vault remotely: ${syncedVault?.id?.value}")
                    return Result.success(syncedVault?.id ?: vault.id)
                } else {
                    Logger.w("Failed to sync vault to remote: ${syncResult.exceptionOrNull()?.message}")
                }
            }
            
            // Fallback: guardar solo localmente
            val entity = vault.vaultToEntity()
            vaultDao.insertVault(entity)
            Logger.d("Vault saved locally: ${vault.id.value}")
            
            Result.success(vault.id)
            
        } catch (e: Exception) {
            Logger.e("Error creating vault: ${vault.name}", throwable = e)
            Result.failure(RepositoryError.DatabaseError("Error al crear el baúl", e))
        }
    }
    
    override suspend fun updateVault(vault: Vault): Result<Unit> {
        return try {
            Logger.d("Updating vault: ${vault.id.value}")
            
            // Validar datos de entrada
            if (vault.name.isBlank()) {
                return Result.failure(RepositoryError.ValidationError("El nombre del baúl no puede estar vacío"))
            }
            
            // Actualizar localmente primero
            val entity = vault.vaultToEntity()
            vaultDao.updateVault(entity)
            
            // Sincronizar con API si hay conectividad
            val isConnected = networkManager.isCurrentlyConnected()
            if (isConnected) {
                val syncResult = vaultSyncManager.syncVaultUpdateToRemote(vault)
                if (syncResult.isFailure) {
                    Logger.w("Failed to sync vault update to remote: ${syncResult.exceptionOrNull()?.message}")
                }
            }
            
            Logger.d("Successfully updated vault: ${vault.id.value}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Logger.e("Error updating vault: ${vault.id.value}", throwable = e)
            Result.failure(RepositoryError.DatabaseError("Error al actualizar el baúl", e))
        }
    }
    
    override suspend fun deleteVault(vaultId: VaultId): Result<Unit> {
        return try {
            Logger.d("Deleting vault: ${vaultId.value}")
            
            // Eliminar localmente primero
            vaultDao.deleteVaultById(vaultId.value)
            
            // Sincronizar eliminación con API si hay conectividad
            val isConnected = networkManager.isCurrentlyConnected()
            if (isConnected) {
                val syncResult = vaultSyncManager.syncVaultDeletionToRemote(vaultId)
                if (syncResult.isFailure) {
                    Logger.w("Failed to sync vault deletion to remote: ${syncResult.exceptionOrNull()?.message}")
                }
            }
            
            Logger.d("Successfully deleted vault: ${vaultId.value}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Logger.e("Error deleting vault: ${vaultId.value}", throwable = e)
            Result.failure(RepositoryError.DatabaseError("Error al eliminar el baúl", e))
        }
    }
    
    override suspend fun joinVault(vaultId: VaultId, userId: UserId): Result<Unit> {
        return try {
            Logger.d("Joining vault: ${vaultId.value} for user: ${userId.value}")
            
            val isConnected = networkManager.isCurrentlyConnected()
            if (!isConnected) {
                return Result.failure(RepositoryError.NetworkError("No hay conexión a internet"))
            }
            
            // TODO: Implementar lógica de unirse a baúl
            // Por ahora solo logueamos la acción
            Logger.d("User ${userId.value} joined vault ${vaultId.value}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Logger.e("Error joining vault: ${vaultId.value}", throwable = e)
            Result.failure(RepositoryError.NetworkError("Error al unirse al baúl", e))
        }
    }
    
    override suspend fun leaveVault(vaultId: VaultId, userId: UserId): Result<Unit> {
        return try {
            Logger.d("Leaving vault: ${vaultId.value} for user: ${userId.value}")
            
            val isConnected = networkManager.isCurrentlyConnected()
            if (!isConnected) {
                return Result.failure(RepositoryError.NetworkError("No hay conexión a internet"))
            }
            
            // TODO: Implementar lógica de salir de baúl
            // Por ahora solo logueamos la acción
            Logger.d("User ${userId.value} left vault ${vaultId.value}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Logger.e("Error leaving vault: ${vaultId.value}", throwable = e)
            Result.failure(RepositoryError.NetworkError("Error al salir del baúl", e))
        }
    }
    
    private fun createDefaultVault(userId: UserId): List<Vault> {
        val defaultVaultId = java.util.UUID.randomUUID().toString()
        Logger.d("Creating default vault with ID: $defaultVaultId for user: ${userId.value}")
        
        val defaultVault = Vault(
            id = VaultId(defaultVaultId),
            name = "Mi Baúl Familiar",
            description = "Baúl principal de recuerdos familiares",
            ownerId = userId,
            memberCount = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        Logger.d("Default vault created: ${defaultVault.name} (${defaultVault.id.value})")
        return listOf(defaultVault)
    }
}