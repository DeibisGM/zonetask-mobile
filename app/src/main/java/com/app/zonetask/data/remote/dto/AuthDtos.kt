package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("device_name")
    val deviceName: String? = null,
    @SerializedName("device_type")
    val deviceType: String? = "mobile"
)

data class AuthResponse(
    @SerializedName("session_token")
    val sessionToken: String = "",
    @SerializedName("refresh_token")
    val refreshToken: String = "",
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    @SerializedName("user")
    val user: UserResponse? = null
)
