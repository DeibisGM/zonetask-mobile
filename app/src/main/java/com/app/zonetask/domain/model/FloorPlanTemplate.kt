package com.app.zonetask.domain.model

import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import java.util.UUID

data class FloorPlanTemplate(
    val templateId: Int,
    val name: String,
    val description: String,
    val defaultColumns: Int,
    val defaultRows: Int,
    val zones: List<TemplateZone>
)

data class TemplateZone(
    val name: String,
    val relativeX: Float,
    val relativeY: Float,
    val relativeWidth: Float,
    val relativeHeight: Float,
    val fillColor: String,
    val strokeColor: String = "#111111",
    val strokeWidth: Float = 2f,
    val opacity: Float = 0.92f,
    val displayOrder: Int
)

fun TemplateZone.toDraft(): PlanZoneDraft = PlanZoneDraft(
    id = UUID.randomUUID().toString(),
    backendId = null,
    name = name,
    x = relativeX,
    y = relativeY,
    width = relativeWidth,
    height = relativeHeight,
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity
)
