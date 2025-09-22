package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTOs para personas del árbol genealógico
 */

data class PersonDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "first_name")
    val firstName: String,
    @Json(name = "last_name")
    val lastName: String,
    @Json(name = "middle_name")
    val middleName: String,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "birth_date")
    val birthDate: String?,
    @Json(name = "death_date")
    val deathDate: String?,
    @Json(name = "birth_place")
    val birthPlace: String,
    @Json(name = "death_place")
    val deathPlace: String,
    @Json(name = "email")
    val email: String,
    @Json(name = "phone")
    val phone: String,
    @Json(name = "address")
    val address: String,
    @Json(name = "photo")
    val photo: String?,
    @Json(name = "documents")
    val documents: List<String>,
    @Json(name = "occupation")
    val occupation: String,
    @Json(name = "notes")
    val notes: String,
    @Json(name = "is_living")
    val isLiving: Boolean,
    @Json(name = "age")
    val age: Int?,
    @Json(name = "vault")
    val vault: String,
    @Json(name = "vault_name")
    val vaultName: String,
    @Json(name = "created_by")
    val createdBy: String,
    @Json(name = "created_by_name")
    val createdByName: String,
    @Json(name = "memories_count")
    val memoriesCount: Int,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class PersonCreateRequest(
    @Json(name = "first_name")
    val firstName: String,
    @Json(name = "last_name")
    val lastName: String,
    @Json(name = "middle_name")
    val middleName: String = "",
    @Json(name = "birth_date")
    val birthDate: String? = null,
    @Json(name = "death_date")
    val deathDate: String? = null,
    @Json(name = "birth_place")
    val birthPlace: String = "",
    @Json(name = "death_place")
    val deathPlace: String = "",
    @Json(name = "email")
    val email: String = "",
    @Json(name = "phone")
    val phone: String = "",
    @Json(name = "address")
    val address: String = "",
    @Json(name = "documents")
    val documents: List<String> = emptyList(),
    @Json(name = "occupation")
    val occupation: String = "",
    @Json(name = "notes")
    val notes: String = "",
    @Json(name = "is_living")
    val isLiving: Boolean = true,
    @Json(name = "vault")
    val vault: String
)


data class RelationCreateRequest(
    @Json(name = "person1")
    val person1: String,
    @Json(name = "person2")
    val person2: String,
    @Json(name = "relation_type")
    val relationType: String,
    @Json(name = "start_date")
    val startDate: String? = null,
    @Json(name = "end_date")
    val endDate: String? = null,
    @Json(name = "notes")
    val notes: String = ""
)

