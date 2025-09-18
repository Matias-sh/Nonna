package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.MemoryDto
import com.cocido.nonna.data.remote.dto.MemoryRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Servicio de API para operaciones con recuerdos sensoriales
 */
interface MemoryApiService {
    
    @GET("vaults/{vaultId}/memories")
    suspend fun getMemories(@Path("vaultId") vaultId: String): List<MemoryDto>
    
    @GET("vaults/{vaultId}/memories/{id}")
    suspend fun getMemory(@Path("vaultId") vaultId: String, @Path("id") id: String): MemoryDto
    
    @POST("vaults/{vaultId}/memories")
    suspend fun createMemory(@Path("vaultId") vaultId: String, @Body request: MemoryRequest): MemoryDto
    
    @PUT("vaults/{vaultId}/memories/{id}")
    suspend fun updateMemory(
        @Path("vaultId") vaultId: String, 
        @Path("id") id: String, 
        @Body request: MemoryRequest
    ): MemoryDto
    
    @DELETE("vaults/{vaultId}/memories/{id}")
    suspend fun deleteMemory(@Path("vaultId") vaultId: String, @Path("id") id: String)
    
    @Multipart
    @POST("uploads/photo")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): ResponseBody
    
    @Multipart
    @POST("uploads/audio")
    suspend fun uploadAudio(@Part audio: MultipartBody.Part): ResponseBody
}


