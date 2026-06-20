package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateChatGroupRequest(
    @SerializedName("name")        val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("imageUrl")    val imageUrl: String?
)