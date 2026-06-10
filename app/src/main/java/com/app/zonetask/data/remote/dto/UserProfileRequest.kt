package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("profile_picture_url")
    val profilePictureUrl: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("birth_date")
    val birthDate: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("onboarding_completed")
    val onboardingCompleted: Boolean? = null
)
