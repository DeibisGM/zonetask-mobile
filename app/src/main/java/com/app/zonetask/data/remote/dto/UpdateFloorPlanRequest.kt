package com.app.zonetask.data.remote.dto

data class UpdateFloorPlanRequest(
    val name: String? = null,
    val canvasWidth: Float? = null,
    val canvasHeight: Float? = null,
    val requestingUserId: Int? = null
)
