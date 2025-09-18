package com.cocido.nonna.data.local.dao

import androidx.room.*
import com.cocido.nonna.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con personas
 */
@Dao
interface PersonDao {
    
    @Query("SELECT * FROM persons ORDER BY fullName ASC")
    fun getAllPersons(): Flow<List<PersonEntity>>
    
    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: String): PersonEntity?
    
    @Query("SELECT * FROM persons WHERE fullName LIKE '%' || :query || '%' ORDER BY fullName ASC")
    fun searchPersons(query: String): Flow<List<PersonEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)
    
    @Update
    suspend fun updatePerson(person: PersonEntity)
    
    @Delete
    suspend fun deletePerson(person: PersonEntity)
    
    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun deletePersonById(id: String)
}


