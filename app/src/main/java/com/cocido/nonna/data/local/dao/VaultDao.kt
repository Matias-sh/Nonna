package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.VaultEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con cofres familiares
 */
@Dao
interface VaultDao {
    
    @Query("SELECT * FROM vaults")
    fun getAllVaults(): Flow<List<VaultEntity>>
    
    @Query("SELECT * FROM vaults WHERE id = :id")
    suspend fun getVaultById(id: String): VaultEntity?
    
    @Query("SELECT * FROM vaults WHERE ownerUid = :ownerUid")
    fun getVaultsByOwner(ownerUid: String): Flow<List<VaultEntity>>
    
    @Query("SELECT * FROM vaults WHERE :uid IN (memberUids)")
    fun getVaultsByMember(uid: String): Flow<List<VaultEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: VaultEntity)
    
    @Update
    suspend fun updateVault(vault: VaultEntity)
    
    @Delete
    suspend fun deleteVault(vault: VaultEntity)
    
    @Query("DELETE FROM vaults WHERE id = :id")
    suspend fun deleteVaultById(id: String)
}


