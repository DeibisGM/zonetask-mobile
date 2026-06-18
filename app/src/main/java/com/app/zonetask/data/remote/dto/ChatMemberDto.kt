package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatMemberDto(
    @SerializedName("memberId")         val memberId: Int,
    @SerializedName("userId")           val userId: Int,
    @SerializedName("firstName")        val firstName: String,
    @SerializedName("lastName")         val lastName: String,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String?,
    @SerializedName("role")             val role: String,
    @SerializedName("status")           val status: String
) {
    val initials: String
        get() {
            val f = firstName.firstOrNull()?.uppercaseChar() ?: ""
            val l = lastName.firstOrNull()?.uppercaseChar() ?: ""
            return "$f$l"
        }

    val fullName: String
        get() = "$firstName $lastName".trim()

    val isActive: Boolean
        get() = status.equals("active", ignoreCase = true)
}