package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con recuerdos
 */
@Dao
interface MemoryDao {
    
    @Query("SELECT * FROM memories WHERE vaultId = :vaultId ORDER BY createdAt DESC")
    fun getMemoriesByVault(vaultId: String): Flow<List<MemoryEntity>>
    
    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: String): MemoryEntity?
    
    @Query("SELECT * FROM memories WHERE vaultId = :vaultId AND (title LIKE '%' || :query || '%' OR transcript LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchMemories(vaultId: String, query: String): Flow<List<MemoryEntity>>
    
    @Query("SELECT * FROM memories WHERE vaultId = :vaultId AND dateTaken BETWEEN :startDate AND :endDate ORDER BY dateTaken ASC")
    fun getMemoriesByDateRange(vaultId: String, startDate: Long, endDate: Long): Flow<List<MemoryEntity>>
    
    @Query("SELECT * FROM memories WHERE vaultId = :vaultId AND syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSyncMemories(vaultId: String): List<MemoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)
    
    @Update
    suspend fun updateMemory(memory: MemoryEntity)
    
    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)
    
    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: String)
}


