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
import kotlinx.coroutines.flow.flow
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
        return flow {
            try {
                Logger.d("Getting user vaults for userId: ${userId.value}")
                
                // Verificar conectividad de red
                val isConnected = networkManager.isCurrentlyConnected()
                
                if (isConnected) {
                    // Intentar sincronizar desde la API
                    val syncResult = vaultSyncManager.syncVaultsFromRemote(userId)
                    if (syncResult.isSuccess) {
                        val vaults = syncResult.getOrNull() ?: emptyList()
                        Logger.d("Successfully synced ${vaults.size} vaults from remote")
                        emit(vaults)
                        return@flow
                    } else {
                        Logger.w("Failed to sync vaults from remote: ${syncResult.exceptionOrNull()?.message}")
                    }
                } else {
                    Logger.w("No network connection, using local cache")
                }
                
                // Fallback a cache local
                vaultDao.getUserVaults(userId.value).collect { entities ->
                    val vaults = entities.map { it.vaultEntityToDomain() }
                    
                    if (vaults.isEmpty()) {
                        Logger.d("No local vaults found, creating default vault")
                        emit(createDefaultVault(userId))
                    } else {
                        Logger.d("Found ${vaults.size} vaults in local cache")
                        emit(vaults)
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("Error getting user vaults", throwable = e)
                emit(createDefaultVault(userId))
            }
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
        return listOf(
            Vault(
                id = VaultId("default_vault_${userId.value}"),
                name = "Mi Baúl Familiar",
                description = "Baúl principal de recuerdos familiares",
                ownerId = userId,
                memberCount = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}