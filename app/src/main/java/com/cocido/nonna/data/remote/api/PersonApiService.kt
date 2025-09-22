package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.PersonDto
import com.cocido.nonna.data.remote.dto.RelationDto
import retrofit2.http.*

/**
 * Servicio de API para personas del árbol genealógico
 */
interface PersonApiService {
    
    @GET("genealogy/persons/")
    suspend fun getPersons(): List<PersonDto>
    
    @POST("genealogy/persons/")
    suspend fun createPerson(@Body person: PersonDto): PersonDto
    
    @GET("genealogy/persons/{id}/")
    suspend fun getPerson(@Path("id") id: String): PersonDto
    
    @PUT("genealogy/persons/{id}/")
    suspend fun updatePerson(@Path("id") id: String, @Body person: PersonDto): PersonDto
    
    @DELETE("genealogy/persons/{id}/")
    suspend fun deletePerson(@Path("id") id: String)
    
    @GET("genealogy/relations/")
    suspend fun getRelations(): List<RelationDto>
    
    @POST("genealogy/relations/")
    suspend fun createRelation(@Body relation: RelationDto): RelationDto
    
    @GET("genealogy/relations/{id}/")
    suspend fun getRelation(@Path("id") id: String): RelationDto
    
    @PUT("genealogy/relations/{id}/")
    suspend fun updateRelation(@Path("id") id: String, @Body relation: RelationDto): RelationDto
    
    @DELETE("genealogy/relations/{id}/")
    suspend fun deleteRelation(@Path("id") id: String)
    
    @GET("genealogy/vaults/{vaultId}/graph/")
    suspend fun getGenealogyGraph(@Path("vaultId") vaultId: String): Map<String, Any>
    
    @GET("genealogy/vaults/{vaultId}/stats/")
    suspend fun getGenealogyStats(@Path("vaultId") vaultId: String): Map<String, Any>
    
    @GET("genealogy/persons/{personId}/family-tree/")
    suspend fun getPersonFamilyTree(@Path("personId") personId: String): Map<String, Any>
}

