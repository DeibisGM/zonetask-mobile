package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PagedMessagesDto(
    @SerializedName("items")           val items: List<ChatMessageDto>,
    @SerializedName("page")            val page: Int,
    @SerializedName("limit")           val pageSize: Int,
    @SerializedName("totalCount")      val totalCount: Int,
    @SerializedName("totalPages")      val totalPages: Int,
    @SerializedName("hasNextPage")     val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)