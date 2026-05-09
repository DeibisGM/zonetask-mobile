package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("username")
    val username: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("displayName")
    val displayName: String = ""
)
