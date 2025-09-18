package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.VaultDto
import com.cocido.nonna.data.remote.dto.VaultRequest
import com.cocido.nonna.data.remote.dto.InviteRequest
import retrofit2.http.*

/**
 * Servicio de API para operaciones con cofres familiares
 */
interface VaultApiService {
    
    @GET("vaults")
    suspend fun getVaults(): List<VaultDto>
    
    @GET("vaults/{id}")
    suspend fun getVault(@Path("id") id: String): VaultDto
    
    @POST("vaults")
    suspend fun createVault(@Body request: VaultRequest): VaultDto
    
    @PUT("vaults/{id}")
    suspend fun updateVault(@Path("id") id: String, @Body request: VaultRequest): VaultDto
    
    @DELETE("vaults/{id}")
    suspend fun deleteVault(@Path("id") id: String)
    
    @POST("vaults/{id}/invite")
    suspend fun inviteMember(@Path("id") id: String, @Body request: InviteRequest)
}


