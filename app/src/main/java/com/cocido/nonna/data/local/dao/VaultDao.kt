package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.VaultEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con baúles
 */
@Dao
interface VaultDao {
    
    @Query("SELECT * FROM vaults ORDER BY createdAt DESC")
    fun getAllVaults(): Flow<List<VaultEntity>>
    
    @Query("SELECT * FROM vaults WHERE id = :vaultId")
    suspend fun getVaultById(vaultId: String): VaultEntity?
    
    @Query("SELECT * FROM vaults WHERE ownerId = :userId ORDER BY createdAt DESC")
    fun getUserVaults(userId: String): Flow<List<VaultEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: VaultEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaults(vaults: List<VaultEntity>)
    
    @Update
    suspend fun updateVault(vault: VaultEntity)
    
    @Delete
    suspend fun deleteVault(vault: VaultEntity)
    
    @Query("DELETE FROM vaults WHERE id = :vaultId")
    suspend fun deleteVaultById(vaultId: String)
}