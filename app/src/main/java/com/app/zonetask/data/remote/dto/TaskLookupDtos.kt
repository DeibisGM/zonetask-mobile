package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LookupOptionResponse(
    // Generic label/value item for dropdowns and checkboxes.
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

data class TaskFormOptionsResponse(
    // Bundles the lookup data needed by the create-task form.
    @SerializedName("spaceId")
    val spaceId: Int,
    @SerializedName("zones")
    val zones: List<LookupOptionResponse> = emptyList(),
    @SerializedName("categories")
    val categories: List<LookupOptionResponse> = emptyList()
)
