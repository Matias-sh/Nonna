package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.PersonDto
import com.cocido.nonna.data.remote.dto.PersonRequest
import com.cocido.nonna.data.remote.dto.RelationDto
import com.cocido.nonna.data.remote.dto.RelationRequest
import retrofit2.http.*

/**
 * Servicio de API para operaciones con personas y relaciones del árbol genealógico
 */
interface PersonApiService {
    
    @GET("vaults/{vaultId}/persons")
    suspend fun getPersons(@Path("vaultId") vaultId: String): List<PersonDto>
    
    @GET("vaults/{vaultId}/persons/{id}")
    suspend fun getPerson(@Path("vaultId") vaultId: String, @Path("id") id: String): PersonDto
    
    @POST("vaults/{vaultId}/persons")
    suspend fun createPerson(@Path("vaultId") vaultId: String, @Body request: PersonRequest): PersonDto
    
    @PUT("vaults/{vaultId}/persons/{id}")
    suspend fun updatePerson(
        @Path("vaultId") vaultId: String, 
        @Path("id") id: String, 
        @Body request: PersonRequest
    ): PersonDto
    
    @DELETE("vaults/{vaultId}/persons/{id}")
    suspend fun deletePerson(@Path("vaultId") vaultId: String, @Path("id") id: String)
    
    @GET("relations")
    suspend fun getRelations(): List<RelationDto>
    
    @POST("relations")
    suspend fun createRelation(@Body request: RelationRequest): RelationDto
    
    @DELETE("relations/{id}")
    suspend fun deleteRelation(@Path("id") id: Long)
}


