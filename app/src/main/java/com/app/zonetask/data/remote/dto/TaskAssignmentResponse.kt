package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TaskAssignmentResponse(
    @SerializedName("assignmentId")
    val assignmentId: Int,
    @SerializedName("assignedAt")
    val assignedAt: String,
    @SerializedName("dueAt")
    val dueAt: String? = null,
    @SerializedName("dueStatusKey")
    val dueStatusKey: String = "none",
    @SerializedName("dueStatusLabel")
    val dueStatusLabel: String = "",
    @SerializedName("status")
    val status: String,
    @SerializedName("isAuto")
    val isAuto: Boolean,
    @SerializedName("rotationRound")
    val rotationRound: Int,
    @SerializedName("assignedUserId")
    val assignedUserId: Int,
    @SerializedName("taskId")
    val taskId: Int
)
