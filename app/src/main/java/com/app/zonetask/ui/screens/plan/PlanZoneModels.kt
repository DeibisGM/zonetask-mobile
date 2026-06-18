package com.app.zonetask.ui.screens.plan

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class PlanZoneDraft(
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val fillColor: String,
    val id: String = UUID.randomUUID().toString(),
    val backendId: Int? = null,
    val strokeColor: String = "#111111",
    val strokeWidth: Float = 2f,
    val opacity: Float = 0.92f
)

data class PlanZoneDraftSnapshot(
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val fillColor: String,
    val id: String,
    val backendId: Int?,
    val strokeColor: String,
    val strokeWidth: Float,
    val opacity: Float
)

fun PlanZoneDraft.toSnapshot(): PlanZoneDraftSnapshot = PlanZoneDraftSnapshot(
    id = id,
    backendId = backendId,
    name = name,
    x = x,
    y = y,
    width = width,
    height = height,
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity
)

fun PlanZoneDraftSnapshot.toDraft(): PlanZoneDraft = PlanZoneDraft(
    id = id,
    backendId = backendId,
    name = name,
    x = x,
    y = y,
    width = width,
    height = height,
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity
)

fun String.asZoneColor(): Color = Color(android.graphics.Color.parseColor(this))

val PlanZonePalette = listOf(
    "#76D6D0",
    "#F7B36E",
    "#8EACFF",
    "#F08BA2",
    "#85D69A",
    "#C68CF1",
    "#B8C2D8"
)

fun createStarterZones(): List<PlanZoneDraft> = listOf(
    PlanZoneDraft(
        name = "Sala",
        backendId = null,
        x = 0.04f,
        y = 0.04f,
        width = 0.42f,
        height = 0.34f,
        fillColor = PlanZonePalette[0]
    ),
    PlanZoneDraft(
        name = "Cocina",
        backendId = null,
        x = 0.54f,
        y = 0.05f,
        width = 0.40f,
        height = 0.28f,
        fillColor = PlanZonePalette[1]
    ),
    PlanZoneDraft(
        name = "Baño",
        backendId = null,
        x = 0.54f,
        y = 0.36f,
        width = 0.18f,
        height = 0.18f,
        fillColor = PlanZonePalette[2]
    ),
    PlanZoneDraft(
        name = "Dormitorio",
        backendId = null,
        x = 0.74f,
        y = 0.38f,
        width = 0.20f,
        height = 0.18f,
        fillColor = PlanZonePalette[3]
    )
)
