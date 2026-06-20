package com.app.zonetask.data.remote.dto

import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import com.google.gson.annotations.SerializedName

data class ZoneResponse(
    @SerializedName("zoneId")
    val zoneId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("posX")
    val posX: Float,

    @SerializedName("posY")
    val posY: Float,

    @SerializedName("width")
    val width: Float,

    @SerializedName("height")
    val height: Float,

    @SerializedName("shapeType")
    val shapeType: String,

    @SerializedName("shapePoints")
    val shapePoints: String? = null,

    @SerializedName("fillColor")
    val fillColor: String,

    @SerializedName("strokeColor")
    val strokeColor: String,

    @SerializedName("strokeWidth")
    val strokeWidth: Float,

    @SerializedName("opacity")
    val opacity: Float,

    @SerializedName("displayOrder")
    val displayOrder: Int,

    @SerializedName("planId")
    val planId: Int
)

data class ZoneRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("posX")
    val posX: Float,

    @SerializedName("posY")
    val posY: Float,

    @SerializedName("width")
    val width: Float,

    @SerializedName("height")
    val height: Float,

    @SerializedName("shapeType")
    val shapeType: String = "rectangle",

    @SerializedName("shapePoints")
    val shapePoints: String? = null,

    @SerializedName("fillColor")
    val fillColor: String,

    @SerializedName("strokeColor")
    val strokeColor: String = "#111111",

    @SerializedName("strokeWidth")
    val strokeWidth: Float = 2f,

    @SerializedName("opacity")
    val opacity: Float = 0.92f,

    @SerializedName("displayOrder")
    val displayOrder: Int
)

fun ZoneResponse.toDraft(canvasWidth: Float, canvasHeight: Float): PlanZoneDraft = PlanZoneDraft(
    backendId = zoneId,
    id = "zone_$zoneId",
    name = name,
    x = (posX / canvasWidth.coerceAtLeast(1f)).coerceIn(0f, 1f),
    y = (posY / canvasHeight.coerceAtLeast(1f)).coerceIn(0f, 1f),
    width = (width / canvasWidth.coerceAtLeast(1f)).coerceIn(0f, 1f),
    height = (height / canvasHeight.coerceAtLeast(1f)).coerceIn(0f, 1f),
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity
)

fun PlanZoneDraft.toRequest(displayOrder: Int, canvasWidth: Float, canvasHeight: Float): ZoneRequest = ZoneRequest(
    name = name.trim(),
    posX = x * canvasWidth,
    posY = y * canvasHeight,
    width = width * canvasWidth,
    height = height * canvasHeight,
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity,
    displayOrder = displayOrder
)
