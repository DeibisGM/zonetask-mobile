package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.app.zonetask.domain.model.FloorPlan

// Backend serializes camelCase at runtime (System.Text.Json defaults).
data class FloorPlanResponse(
    @SerializedName("planId")
    val planId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("canvasWidth")
    val canvasWidth: Float,

    @SerializedName("canvasHeight")
    val canvasHeight: Float,

    @SerializedName("spaceId")
    val spaceId: Int,

    @SerializedName("templateId")
    val templateId: Int? = null
)

fun FloorPlanResponse.toDomain(): FloorPlan = FloorPlan(
    planId       = planId,
    name         = name,
    canvasWidth  = canvasWidth,
    canvasHeight = canvasHeight,
    spaceId      = spaceId,
    templateId   = templateId
)
