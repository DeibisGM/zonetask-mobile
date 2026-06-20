package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("chatMessageId") val chatMessageId: Int,
    @SerializedName("content")       val content: String,
    @SerializedName("spaceId")       val spaceId: Int,
    @SerializedName("senderId")      val senderId: Int,
    @SerializedName("senderDisplayName")      val senderDisplayName: String,
    @SerializedName("senderProfilePictureUrl") val senderProfilePictureUrl: String?,
    @SerializedName("createdAt")     val createdAt: String
) {
    val initials: String get() {
        val parts = senderDisplayName.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
}