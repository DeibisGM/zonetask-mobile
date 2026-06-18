package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SpaceStatisticsResponse(
    @SerializedName("spaceId")        val spaceId: Int,
    @SerializedName("period")         val period: String,
    @SerializedName("dateFrom")       val dateFrom: String,
    @SerializedName("dateTo")         val dateTo: String,
    @SerializedName("totalAssigned")  val totalAssigned: Int,
    @SerializedName("completedTasks") val completedTasks: Int,
    @SerializedName("overdueTasks")   val overdueTasks: Int,
    @SerializedName("pendingTasks")   val pendingTasks: Int,
    @SerializedName("completionRate") val completionRate: Double
)
