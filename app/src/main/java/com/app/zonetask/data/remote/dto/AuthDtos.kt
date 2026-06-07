package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("token_cfm")
    val tokenCfm: String? = null,
    @SerializedName("device_name")
    val deviceName: String? = null,
    @SerializedName("device_type")
    val deviceType: String? = "mobile"
)

data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("profile_picture_url")
    val profilePictureUrl: String? = null,
    @SerializedName("birth_date")
    val birthDate: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("token_cfm")
    val tokenCfm: String? = null
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

data class RegisterResponse(
    @SerializedName("user")
    val user: UserResponse? = null,
    @SerializedName("email_verification_sent")
    val emailVerificationSent: Boolean = false
)
