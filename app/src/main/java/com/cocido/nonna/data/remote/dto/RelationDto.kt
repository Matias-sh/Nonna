package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

data class RelationDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "person1")
    val person1: String,
    @Json(name = "person1_name")
    val person1Name: String,
    @Json(name = "person2")
    val person2: String,
    @Json(name = "person2_name")
    val person2Name: String,
    @Json(name = "relation_type")
    val relationType: String,
    @Json(name = "start_date")
    val startDate: String?,
    @Json(name = "end_date")
    val endDate: String?,
    @Json(name = "notes")
    val notes: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)



