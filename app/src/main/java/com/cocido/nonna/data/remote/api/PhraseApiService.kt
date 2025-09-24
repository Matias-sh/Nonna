package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.PhraseDto
import com.cocido.nonna.data.remote.dto.PhrasePlaybackDto
import retrofit2.http.*

/**
 * Servicio de API para frases de conversación
 */
interface PhraseApiService {
    
    @GET("conversation/phrases/")
    suspend fun getPhrases(): List<PhraseDto>
    
    @POST("conversation/phrases/")
    suspend fun createPhrase(@Body phrase: PhraseDto): PhraseDto
    
    @GET("conversation/phrases/{id}/")
    suspend fun getPhrase(@Path("id") id: String): PhraseDto
    
    @PUT("conversation/phrases/{id}/")
    suspend fun updatePhrase(@Path("id") id: String, @Body phrase: PhraseDto): PhraseDto
    
    @DELETE("conversation/phrases/{id}/")
    suspend fun deletePhrase(@Path("id") id: String)
    
    @POST("conversation/phrases/{id}/play/")
    suspend fun playPhrase(@Path("id") id: String): PhrasePlaybackDto
    
    @POST("conversation/phrases/{id}/favorite/")
    suspend fun toggleFavoritePhrase(@Path("id") id: String): PhraseDto
    
    @GET("conversation/vaults/{vaultId}/stats/")
    suspend fun getPhraseStats(@Path("vaultId") vaultId: String): Map<String, Any>
    
    @GET("conversation/vaults/{vaultId}/random/")
    suspend fun getRandomPhrases(@Path("vaultId") vaultId: String, @Query("count") count: Int = 5): List<PhraseDto>
}


