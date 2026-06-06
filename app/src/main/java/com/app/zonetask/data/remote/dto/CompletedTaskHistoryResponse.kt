package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CompletedTaskHistoryResponse(
    @SerializedName("task_title")      val taskTitle: String,
    @SerializedName("zone_name")       val zoneName: String?,
    @SerializedName("completed_by")    val completedBy: String,
    @SerializedName("completed_at")    val completedAt: String,
    @SerializedName("completion_type") val completionType: String
)
