package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.model.UserId
import com.google.gson.annotations.SerializedName

/**
 * DTO para baúles desde la API
 */
data class VaultDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("owner")
    val owner: String,
    
    @SerializedName("owner_name")
    val ownerName: String?,
    
    @SerializedName("member_count")
    val memberCount: Int,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Extensión para convertir DTO a modelo de dominio
 */
fun VaultDto.toDomain(): Vault {
    return Vault(
        id = VaultId(id),
        name = name,
        description = description,
        ownerId = UserId(owner),
        memberCount = memberCount,
        createdAt = parseDate(createdAt) ?: System.currentTimeMillis(),
        updatedAt = parseDate(updatedAt) ?: System.currentTimeMillis()
    )
}

private fun parseDate(dateString: String): Long? {
    return try {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            .parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}