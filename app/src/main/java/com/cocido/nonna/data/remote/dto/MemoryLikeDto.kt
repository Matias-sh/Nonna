package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para likes de recuerdos
 */
data class MemoryLikeDto(
    @SerializedName("user")
    val user: String,
    
    @SerializedName("user_name")
    val userName: String?,
    
    @SerializedName("created_at")
    val createdAt: String
)

