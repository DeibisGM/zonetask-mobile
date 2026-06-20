package com.app.zonetask.data.remote.dto

import com.app.zonetask.domain.model.FloorPlanTemplate
import com.app.zonetask.domain.model.TemplateZone
import com.google.gson.annotations.SerializedName

data class TemplateZoneDto(
    @SerializedName("name")        val name: String,
    @SerializedName("relativeX")   val relativeX: Float,
    @SerializedName("relativeY")   val relativeY: Float,
    @SerializedName("relativeWidth")  val relativeWidth: Float,
    @SerializedName("relativeHeight") val relativeHeight: Float,
    @SerializedName("fillColor")   val fillColor: String,
    @SerializedName("strokeColor") val strokeColor: String = "#111111",
    @SerializedName("strokeWidth") val strokeWidth: Float = 2f,
    @SerializedName("opacity")     val opacity: Float = 0.92f,
    @SerializedName("displayOrder") val displayOrder: Int
)

data class FloorPlanTemplateResponse(
    @SerializedName("templateId")     val templateId: Int,
    @SerializedName("name")           val name: String,
    @SerializedName("description")    val description: String,
    @SerializedName("defaultColumns") val defaultColumns: Int,
    @SerializedName("defaultRows")    val defaultRows: Int,
    @SerializedName("zones")          val zones: List<TemplateZoneDto>
)

fun FloorPlanTemplateResponse.toDomain(): FloorPlanTemplate = FloorPlanTemplate(
    templateId = templateId,
    name = name,
    description = description,
    defaultColumns = defaultColumns,
    defaultRows = defaultRows,
    zones = zones.map { z ->
        TemplateZone(
            name = z.name,
            relativeX = z.relativeX,
            relativeY = z.relativeY,
            relativeWidth = z.relativeWidth,
            relativeHeight = z.relativeHeight,
            fillColor = z.fillColor,
            strokeColor = z.strokeColor,
            strokeWidth = z.strokeWidth,
            opacity = z.opacity,
            displayOrder = z.displayOrder
        )
    }
)
