package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatGroupResponse(
    @SerializedName("chatId")      val chatId: Int,
    @SerializedName("spaceId")     val spaceId: Int,
    @SerializedName("name")        val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("imageUrl")    val imageUrl: String?
)