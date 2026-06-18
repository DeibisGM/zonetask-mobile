package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PagedResponse<T>(
    @SerializedName("items")      val items: List<T>,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("page")       val page: Int,
    @SerializedName("limit")      val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)
