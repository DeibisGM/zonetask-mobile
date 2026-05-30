package com.app.zonetask.domain.model

data class FloorPlan(
    val planId: Int,
    val name: String,
    val canvasWidth: Float,
    val canvasHeight: Float,
    val spaceId: Int,
    val templateId: Int? = null
)
