package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.PhraseDto
import com.cocido.nonna.data.remote.dto.PhraseRequest
import retrofit2.http.*

/**
 * Servicio de API para operaciones con frases típicas familiares
 */
interface PhraseApiService {
    
    @GET("vaults/{vaultId}/phrases")
    suspend fun getPhrases(@Path("vaultId") vaultId: String): List<PhraseDto>
    
    @GET("vaults/{vaultId}/phrases/{id}")
    suspend fun getPhrase(@Path("vaultId") vaultId: String, @Path("id") id: String): PhraseDto
    
    @POST("vaults/{vaultId}/phrases")
    suspend fun createPhrase(@Path("vaultId") vaultId: String, @Body request: PhraseRequest): PhraseDto
    
    @PUT("vaults/{vaultId}/phrases/{id}")
    suspend fun updatePhrase(
        @Path("vaultId") vaultId: String, 
        @Path("id") id: String, 
        @Body request: PhraseRequest
    ): PhraseDto
    
    @DELETE("vaults/{vaultId}/phrases/{id}")
    suspend fun deletePhrase(@Path("vaultId") vaultId: String, @Path("id") id: String)
}


