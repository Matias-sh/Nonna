package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con frases de conversación
 */
@Dao
interface PhraseDao {
    
    @Query("SELECT * FROM phrases ORDER BY createdAt DESC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>
    
    @Query("SELECT * FROM phrases WHERE id = :phraseId")
    suspend fun getPhraseById(phraseId: String): PhraseEntity?
    
    @Query("SELECT * FROM phrases WHERE personId = :personId ORDER BY createdAt DESC")
    fun getPhrasesByPerson(personId: String): Flow<List<PhraseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)
    
    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)
    
    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)
    
    @Query("DELETE FROM phrases WHERE id = :phraseId")
    suspend fun deletePhraseById(phraseId: String)
}