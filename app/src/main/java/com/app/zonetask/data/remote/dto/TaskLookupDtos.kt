package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LookupOptionResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

data class TaskFormOptionsResponse(
    @SerializedName("spaceId")
    val spaceId: Int,
    @SerializedName("zones")
    val zones: List<LookupOptionResponse> = emptyList(),
    @SerializedName("categories")
    val categories: List<LookupOptionResponse> = emptyList()
)
