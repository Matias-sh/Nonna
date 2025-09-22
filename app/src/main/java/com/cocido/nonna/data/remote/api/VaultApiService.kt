package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.VaultDto
import com.cocido.nonna.data.remote.dto.VaultRequest
import retrofit2.http.*

/**
 * Servicio de API para operaciones con baúles
 */
interface VaultApiService {
    
    @GET("vaults/")
    suspend fun getUserVaults(): List<VaultDto>
    
    @GET("vaults/")
    suspend fun getUserVaults(@Query("user_id") userId: String): List<VaultDto>
    
    @GET("vaults/{id}/")
    suspend fun getVault(@Path("id") id: String): VaultDto
    
    @POST("vaults/")
    suspend fun createVault(@Body request: VaultRequest): VaultDto
    
    @PUT("vaults/{id}/")
    suspend fun updateVault(@Path("id") id: String, @Body request: VaultRequest): VaultDto
    
    @DELETE("vaults/{id}/")
    suspend fun deleteVault(@Path("id") id: String)
    
    @POST("vaults/{id}/join/")
    suspend fun joinVault(@Path("id") id: String, @Query("user_id") userId: String)
    
    @POST("vaults/{id}/leave/")
    suspend fun leaveVault(@Path("id") id: String, @Query("user_id") userId: String)
}