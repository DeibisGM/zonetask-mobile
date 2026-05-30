package com.app.zonetask.data.remote.dto

data class CreateFloorPlanRequest(
    val name: String,
    val canvasWidth: Float,
    val canvasHeight: Float,
    val spaceId: Int,
    val templateId: Int? = null,
    val requestingUserId: Int? = null
)
