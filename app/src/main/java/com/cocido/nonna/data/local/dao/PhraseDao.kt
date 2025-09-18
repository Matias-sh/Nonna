package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con frases típicas familiares
 */
@Dao
interface PhraseDao {
    
    @Query("SELECT * FROM phrases WHERE vaultId = :vaultId ORDER BY createdAt DESC")
    fun getPhrasesByVault(vaultId: String): Flow<List<PhraseEntity>>
    
    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getPhraseById(id: String): PhraseEntity?
    
    @Query("SELECT * FROM phrases WHERE vaultId = :vaultId AND text LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPhrases(vaultId: String, query: String): Flow<List<PhraseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)
    
    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)
    
    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)
    
    @Query("DELETE FROM phrases WHERE id = :id")
    suspend fun deletePhraseById(id: String)
}


