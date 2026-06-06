package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserReportEntry(
    @SerializedName("rank")           val rank: Int,
    @SerializedName("userId")         val userId: Int,
    @SerializedName("username")       val username: String,
    @SerializedName("fullName")       val fullName: String,
    @SerializedName("totalAssigned")  val totalAssigned: Int,
    @SerializedName("completedTasks") val completedTasks: Int,
    @SerializedName("overdueTasks")   val overdueTasks: Int,
    @SerializedName("pendingTasks")   val pendingTasks: Int,
    @SerializedName("completionRate") val completionRate: Double
)

data class SpaceUserReportsResponse(
    @SerializedName("spaceId")       val spaceId: Int,
    @SerializedName("period")        val period: String,
    @SerializedName("dateFrom")      val dateFrom: String,
    @SerializedName("dateTo")        val dateTo: String,
    @SerializedName("sortBy")        val sortBy: String,
    @SerializedName("totalMembers")  val totalMembers: Int,
    @SerializedName("users")         val users: List<UserReportEntry>
)
