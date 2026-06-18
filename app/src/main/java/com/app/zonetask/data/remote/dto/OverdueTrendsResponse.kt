package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OverdueTrendBucket(
    @SerializedName("periodStart")  val periodStart: String,
    @SerializedName("label")        val label: String,
    @SerializedName("dueCount")     val dueCount: Int,
    @SerializedName("overdueCount") val overdueCount: Int,
    @SerializedName("overdueRate")  val overdueRate: Double
)

data class OverdueByUserEntry(
    @SerializedName("userId")       val userId: Int,
    @SerializedName("username")     val username: String,
    @SerializedName("fullName")     val fullName: String,
    @SerializedName("dueCount")     val dueCount: Int,
    @SerializedName("overdueCount") val overdueCount: Int,
    @SerializedName("overdueRate")  val overdueRate: Double
)

data class OverdueByZoneEntry(
    @SerializedName("zoneId")       val zoneId: Int?,
    @SerializedName("zoneName")     val zoneName: String,
    @SerializedName("dueCount")     val dueCount: Int,
    @SerializedName("overdueCount") val overdueCount: Int,
    @SerializedName("overdueRate")  val overdueRate: Double
)

data class OverdueTrendsResponse(
    @SerializedName("spaceId")      val spaceId: Int,
    @SerializedName("period")       val period: String,
    @SerializedName("dateFrom")     val dateFrom: String,
    @SerializedName("dateTo")       val dateTo: String,
    @SerializedName("interval")     val interval: String,
    @SerializedName("totalDue")     val totalDue: Int,
    @SerializedName("totalOverdue") val totalOverdue: Int,
    @SerializedName("overdueRate")  val overdueRate: Double,
    @SerializedName("buckets")      val buckets: List<OverdueTrendBucket>,
    @SerializedName("byUser")       val byUser: List<OverdueByUserEntry>,
    @SerializedName("byZone")       val byZone: List<OverdueByZoneEntry>
)
