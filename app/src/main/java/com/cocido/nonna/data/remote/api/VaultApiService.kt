package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.VaultDto
import com.cocido.nonna.data.remote.dto.VaultMemberDto
import retrofit2.http.*

/**
 * Servicio de API para cofres (vaults)
 */
interface VaultApiService {
    
    @GET("auth/vaults/")
    suspend fun getVaults(): List<VaultDto>
    
    @POST("auth/vaults/")
    suspend fun createVault(@Body vault: VaultDto): VaultDto
    
    @GET("auth/vaults/{id}/")
    suspend fun getVault(@Path("id") id: String): VaultDto
    
    @PUT("auth/vaults/{id}/")
    suspend fun updateVault(@Path("id") id: String, @Body vault: VaultDto): VaultDto
    
    @DELETE("auth/vaults/{id}/")
    suspend fun deleteVault(@Path("id") id: String)
    
    @GET("auth/vaults/{id}/members/")
    suspend fun getVaultMembers(@Path("id") id: String): List<VaultMemberDto>
    
    @POST("auth/vaults/{id}/members/")
    suspend fun addVaultMember(@Path("id") id: String, @Body member: VaultMemberDto): VaultMemberDto
    
    @PUT("auth/vaults/{id}/members/{memberId}/")
    suspend fun updateVaultMember(@Path("id") id: String, @Path("memberId") memberId: Int, @Body member: VaultMemberDto): VaultMemberDto
    
    @DELETE("auth/vaults/{id}/members/{memberId}/")
    suspend fun removeVaultMember(@Path("id") id: String, @Path("memberId") memberId: Int)
}