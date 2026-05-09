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
    @SerializedName("recurrenceRule")
    val recurrenceRule: String? = null,
    @SerializedName("scheduledTime")
    val scheduledTime: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("rotating")
    val rotating: Boolean = false,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("reminderEnabled")
    val reminderEnabled: Boolean = false,
    @SerializedName("reminderMinutes")
    val reminderMinutes: Int = 30,
    @SerializedName("requiresProof")
    val requiresProof: Boolean = false,
    @SerializedName("requiresDescription")
    val requiresDescription: Boolean = false,
    @SerializedName("estimatedMinutes")
    val estimatedMinutes: Int? = null,
    @SerializedName("createdBy")
    val createdBy: Int,
    @SerializedName("categoryId")
    val categoryId: Int? = null,
    @SerializedName("spaceId")
    val spaceId: Int,
    @SerializedName("zoneId")
    val zoneId: Int? = null,
    @SerializedName("objectId")
    val objectId: Int? = null,
    @SerializedName("objectIds")
    val objectIds: List<Int> = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
