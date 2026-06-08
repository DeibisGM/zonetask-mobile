package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("username")
    val username: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("displayName")
    val displayName: String = "",
    @SerializedName("profilePictureUrl")
    val profilePictureUrl: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("birthDate")
    val birthDate: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("onboardingCompleted")
    val onboardingCompleted: Boolean? = null,
    @SerializedName("emailVerified")
    val emailVerified: Boolean? = null
)
