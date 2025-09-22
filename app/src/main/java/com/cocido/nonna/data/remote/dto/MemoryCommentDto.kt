package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para comentarios de recuerdos
 */
data class MemoryCommentDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("user")
    val user: String,
    
    @SerializedName("user_name")
    val userName: String?,
    
    @SerializedName("user_avatar")
    val userAvatar: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)