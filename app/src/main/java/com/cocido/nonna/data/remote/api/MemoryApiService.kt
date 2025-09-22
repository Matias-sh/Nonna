package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.MemoryDto
import com.cocido.nonna.data.remote.dto.MemoryRequest
import com.cocido.nonna.data.remote.dto.MemoryCommentDto
import com.cocido.nonna.data.remote.dto.MemoryLikeDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Servicio de API para operaciones con recuerdos sensoriales
 */
interface MemoryApiService {
    
    @GET("memories/")
    suspend fun getMemories(): List<MemoryDto>
    
    @GET("memories/{id}/")
    suspend fun getMemory(@Path("id") id: String): MemoryDto
    
    @POST("memories/")
    suspend fun createMemory(@Body request: MemoryRequest): MemoryDto
    
    @PUT("memories/{id}/")
    suspend fun updateMemory(@Path("id") id: String, @Body request: MemoryRequest): MemoryDto
    
    @DELETE("memories/{id}/")
    suspend fun deleteMemory(@Path("id") id: String)
    
    @GET("memories/timeline/")
    suspend fun getTimeline(@Query("year") year: Int? = null): List<MemoryDto>
    
    @GET("memories/stats/")
    suspend fun getMemoryStats(): Map<String, Any>
    
    @GET("memories/{id}/comments/")
    suspend fun getMemoryComments(@Path("id") id: String): List<MemoryCommentDto>
    
    @POST("memories/{id}/comments/")
    suspend fun createMemoryComment(@Path("id") id: String, @Body comment: MemoryCommentDto): MemoryCommentDto
    
    @POST("memories/{id}/like/")
    suspend fun likeMemory(@Path("id") id: String): ResponseBody
    
    @DELETE("memories/{id}/like/")
    suspend fun unlikeMemory(@Path("id") id: String): ResponseBody
    
    @Multipart
    @POST("uploads/photo")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): ResponseBody
    
    @Multipart
    @POST("uploads/audio")
    suspend fun uploadAudio(@Part audio: MultipartBody.Part): ResponseBody
}


