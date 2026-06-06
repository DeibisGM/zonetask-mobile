package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PagedResponse<T>(
    @SerializedName("items")       val items: List<T>,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page")        val page: Int,
    @SerializedName("limit")       val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)
