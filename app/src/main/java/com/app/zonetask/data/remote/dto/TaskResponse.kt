package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    @SerializedName("taskId")
    val taskId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("frequency")
    val frequency: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("scheduledTime")
    val scheduledTime: String?,
    @SerializedName("startDate")
    val startDate: String?,
    @SerializedName("endDate")
    val endDate: String?,
    @SerializedName("createdBy")
    val createdBy: Int,
    @SerializedName("zoneId")
    val zoneId: Int? = null
)
