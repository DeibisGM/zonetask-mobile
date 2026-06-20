package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @SerializedName("content")  val content: String,
    @SerializedName("senderId") val senderId: Int
)