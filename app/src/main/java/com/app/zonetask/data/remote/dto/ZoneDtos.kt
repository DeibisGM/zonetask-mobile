package com.app.zonetask.data.remote.dto

import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import com.app.zonetask.ui.screens.plan.PlanZoneObjectDraft
import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

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
    val planId: Int,

    @SerializedName("objects")
    val objects: List<ZoneLayoutObjectResponse> = emptyList()
)

data class ZoneLayoutObjectResponse(
    @SerializedName("objectId") val objectId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("objectType") val objectType: String,
    @SerializedName("posX") val posX: Float,
    @SerializedName("posY") val posY: Float,
    @SerializedName("width") val width: Float,
    @SerializedName("height") val height: Float,
    @SerializedName("rotation") val rotation: Float = 0f,
    @SerializedName("displayOrder") val displayOrder: Int
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
    val displayOrder: Int,

    @SerializedName("objects")
    val objects: List<ZoneLayoutObjectRequest> = emptyList()
)

data class ZoneLayoutObjectRequest(
    @SerializedName("objectId") val objectId: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("objectType") val objectType: String,
    @SerializedName("posX") val posX: Float,
    @SerializedName("posY") val posY: Float,
    @SerializedName("width") val width: Float,
    @SerializedName("height") val height: Float,
    @SerializedName("rotation") val rotation: Float = 0f,
    @SerializedName("displayOrder") val displayOrder: Int
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
    opacity = opacity,
    objects = objects.map { item ->
        PlanZoneObjectDraft(
            backendId = item.objectId, id = "object_${item.objectId}", name = item.name,
            objectType = item.objectType, column = item.posX.roundToInt(), row = item.posY.roundToInt(),
            spanColumns = item.width.roundToInt().coerceAtLeast(1), spanRows = item.height.roundToInt().coerceAtLeast(1), rotationDegrees = item.rotation.roundToInt()
        )
    }
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
    displayOrder = displayOrder,
    objects = objects.mapIndexed { index, item ->
        ZoneLayoutObjectRequest(item.backendId, item.name, item.objectType, item.column.toFloat(), item.row.toFloat(), item.spanColumns.toFloat(), item.spanRows.toFloat(), item.rotationDegrees.toFloat(), index)
    }
)
