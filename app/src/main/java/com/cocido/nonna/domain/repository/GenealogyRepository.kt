package com.cocido.nonna.domain.repository

import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones del árbol genealógico
 */
interface GenealogyRepository {
    
    /**
     * Obtiene todas las personas de un vault
     */
    fun getPersons(vaultId: VaultId): Flow<List<Person>>
    
    /**
     * Obtiene una persona por su ID
     */
    suspend fun getPerson(personId: PersonId): Person?
    
    /**
     * Crea una nueva persona
     */
    suspend fun createPerson(person: Person): Result<Person>
    
    /**
     * Actualiza una persona existente
     */
    suspend fun updatePerson(person: Person): Result<Person>
    
    /**
     * Elimina una persona
     */
    suspend fun deletePerson(personId: PersonId): Result<Unit>
    
    /**
     * Obtiene todas las relaciones de un vault
     */
    fun getRelations(vaultId: VaultId): Flow<List<Relation>>
    
    /**
     * Crea una nueva relación
     */
    suspend fun createRelation(relation: Relation): Result<Relation>
    
    /**
     * Actualiza una relación existente
     */
    suspend fun updateRelation(relation: Relation): Result<Relation>
    
    /**
     * Elimina una relación
     */
    suspend fun deleteRelation(relation: Relation): Result<Unit>
    
    /**
     * Obtiene el grafo completo del árbol genealógico
     */
    fun getGenealogyGraph(vaultId: VaultId): Flow<Pair<List<Person>, List<Relation>>>
}
