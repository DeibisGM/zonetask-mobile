package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RotationHistoryResponse(
    @SerializedName("logId")
    val logId: Int,
    @SerializedName("rotationRound")
    val rotationRound: Int,
    @SerializedName("triggerReason")
    val triggerReason: String,
    @SerializedName("triggeredAt")
    val triggeredAt: String,
    @SerializedName("taskTitle")
    val taskTitle: String,
    @SerializedName("zoneName")
    val zoneName: String? = null,
    @SerializedName("fromUserId")
    val fromUserId: Int? = null,
    @SerializedName("fromUsername")
    val fromUsername: String? = null,
    @SerializedName("fromDisplayName")
    val fromDisplayName: String? = null,
    @SerializedName("toUserId")
    val toUserId: Int,
    @SerializedName("toUsername")
    val toUsername: String,
    @SerializedName("toDisplayName")
    val toDisplayName: String,
    @SerializedName("taskId")
    val taskId: Int
)
