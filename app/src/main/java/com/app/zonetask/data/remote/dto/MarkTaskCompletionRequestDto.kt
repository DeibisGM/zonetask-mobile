package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

// Minimal payload for completion: the API uses this id only to confirm the assignment belongs to the caller.
data class MarkTaskCompletionRequestDto(
    @SerializedName("requestingUserId")
    val requestingUserId: Int
)
