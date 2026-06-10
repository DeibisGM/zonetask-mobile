package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CompletedTaskHistoryResponse(
    @SerializedName("taskTitle")              val taskTitle: String?,
    @SerializedName("zoneName")               val zoneName: String?,
    @SerializedName("completedByDisplayName") val completedBy: String?,
    @SerializedName("completedAt")            val completedAt: String?,
    @SerializedName("completionType")         val completionType: String?
)
