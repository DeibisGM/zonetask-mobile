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

fun ZoneResponse.toDraft(): PlanZoneDraft = PlanZoneDraft(
    backendId = zoneId,
    id = "zone_$zoneId",
    name = name,
    x = posX,
    y = posY,
    width = width,
    height = height,
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity
)

fun PlanZoneDraft.toRequest(displayOrder: Int): ZoneRequest = ZoneRequest(
    name = name.trim(),
    posX = x,
    posY = y,
    width = width,
    height = height,
    fillColor = fillColor,
    strokeColor = strokeColor,
    strokeWidth = strokeWidth,
    opacity = opacity,
    displayOrder = displayOrder
)
