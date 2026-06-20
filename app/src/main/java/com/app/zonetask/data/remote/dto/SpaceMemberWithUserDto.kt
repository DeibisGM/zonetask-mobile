package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SpaceMemberWithUserDto(
    @SerializedName("memberId")           val memberId: Int,
    @SerializedName("userId")             val userId: Int,
    @SerializedName("firstName")          val firstName: String,
    @SerializedName("lastName")           val lastName: String?,
    @SerializedName("displayName")        val displayName: String,
    @SerializedName("profilePictureUrl")  val profilePictureUrl: String?,
    @SerializedName("role")               val role: String,
    @SerializedName("status")             val status: String,
    @SerializedName("joinedAt")           val joinedAt: String?
) {
    val initials: String get() {
        val parts = displayName.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
}