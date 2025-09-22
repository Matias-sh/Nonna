package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.domain.model.Vault
import com.google.gson.annotations.SerializedName

/**
 * DTO para crear/actualizar baúles
 */
data class VaultRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null
)

/**
 * Extensión para convertir modelo de dominio a request
 */
fun Vault.toRequest(): VaultRequest {
    return VaultRequest(
        name = name,
        description = description
    )
}