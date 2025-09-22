package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.*
import retrofit2.http.*

interface VaultApiService {
    
    @GET("auth/vaults/")
    suspend fun getVaults(): List<VaultDto>

    @POST("auth/vaults/")
    suspend fun createVault(@Body request: CreateVaultRequest): VaultDto

    @GET("auth/vaults/{id}/")
    suspend fun getVaultById(@Path("id") id: String): VaultDto

    @PUT("auth/vaults/{id}/")
    suspend fun updateVault(@Path("id") id: String, @Body request: CreateVaultRequest): VaultDto

    @DELETE("auth/vaults/{id}/")
    suspend fun deleteVault(@Path("id") id: String)

    @POST("auth/vaults/join/")
    suspend fun joinVault(@Body request: JoinVaultRequest): VaultDto

    @GET("auth/vaults/{id}/members/")
    suspend fun getVaultMembers(@Path("id") id: String): List<VaultMemberDto>

    @POST("auth/vaults/{id}/members/")
    suspend fun inviteMember(@Path("id") id: String, @Body request: InviteMemberRequest): VaultMemberDto

    @DELETE("auth/vaults/{id}/members/{member_id}/")
    suspend fun removeMember(@Path("id") id: String, @Path("member_id") memberId: Int)

    @PUT("auth/vaults/{id}/members/{member_id}/")
    suspend fun updateMemberRole(@Path("id") id: String, @Path("member_id") memberId: Int, @Body request: Map<String, String>): VaultMemberDto
}