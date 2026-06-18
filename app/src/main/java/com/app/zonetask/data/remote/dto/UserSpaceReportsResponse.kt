package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SpaceReportEntry(
    @SerializedName("rank")           val rank: Int,
    @SerializedName("spaceId")        val spaceId: Int,
    @SerializedName("spaceName")      val spaceName: String,
    @SerializedName("spaceType")      val spaceType: String,
    @SerializedName("coverImageUrl")  val coverImageUrl: String?,
    @SerializedName("role")           val role: String,
    @SerializedName("totalAssigned")  val totalAssigned: Int,
    @SerializedName("completedTasks") val completedTasks: Int,
    @SerializedName("overdueTasks")   val overdueTasks: Int,
    @SerializedName("pendingTasks")   val pendingTasks: Int,
    @SerializedName("completionRate") val completionRate: Double
)

data class UserSpaceReportsResponse(
    @SerializedName("userId")         val userId: Int,
    @SerializedName("period")         val period: String,
    @SerializedName("dateFrom")       val dateFrom: String,
    @SerializedName("dateTo")         val dateTo: String,
    @SerializedName("sortBy")         val sortBy: String,
    @SerializedName("totalSpaces")    val totalSpaces: Int,
    @SerializedName("totalAssigned")  val totalAssigned: Int,
    @SerializedName("completedTasks") val completedTasks: Int,
    @SerializedName("overdueTasks")   val overdueTasks: Int,
    @SerializedName("pendingTasks")   val pendingTasks: Int,
    @SerializedName("completionRate") val completionRate: Double,
    @SerializedName("spaces")         val spaces: List<SpaceReportEntry>
)
