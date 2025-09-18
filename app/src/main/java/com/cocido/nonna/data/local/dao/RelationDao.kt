package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.RelationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con relaciones familiares
 */
@Dao
interface RelationDao {
    
    @Query("SELECT * FROM relations")
    fun getAllRelations(): Flow<List<RelationEntity>>
    
    @Query("SELECT * FROM relations WHERE fromPersonId = :personId OR toPersonId = :personId")
    fun getRelationsByPerson(personId: String): Flow<List<RelationEntity>>
    
    @Query("SELECT * FROM relations WHERE fromPersonId = :fromId AND toPersonId = :toId")
    suspend fun getRelation(fromId: String, toId: String): RelationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: RelationEntity)
    
    @Update
    suspend fun updateRelation(relation: RelationEntity)
    
    @Delete
    suspend fun deleteRelation(relation: RelationEntity)
    
    @Query("DELETE FROM relations WHERE fromPersonId = :fromId AND toPersonId = :toId")
    suspend fun deleteRelation(fromId: String, toId: String)
}


