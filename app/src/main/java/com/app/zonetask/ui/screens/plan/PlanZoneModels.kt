package com.app.zonetask.ui.screens.plan

import androidx.compose.ui.graphics.Color
import java.util.UUID
import kotlin.math.roundToInt

/** Four fine grid cells make one real-world metre. */
const val SUBCELLS_PER_METER = 4

data class ZoneObjectCatalogItem(
    val type: String,
    val name: String,
    val spanColumns: Int,
    val spanRows: Int
)

val ZoneObjectCatalog = listOf(
    ZoneObjectCatalogItem("sofa", "Sofa", 8, 4),
    ZoneObjectCatalogItem("bed", "Bed", 4, 8),
    ZoneObjectCatalogItem("table_with_chairs", "Table and chairs", 8, 8)
)

data class FloorGridSpec(
    val columns: Int = 120,
    val rows: Int = 120
) {
    val normalizedCellWidth: Float get() = 1f / columns.coerceAtLeast(1)
    val normalizedCellHeight: Float get() = 1f / rows.coerceAtLeast(1)
}

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
    val opacity: Float = 0.92f,
    val column: Int? = null,
    val row: Int? = null,
    val spanColumns: Int? = null,
    val spanRows: Int? = null,
    val objects: List<PlanZoneObjectDraft> = emptyList()
)

data class PlanZoneObjectDraft(
    val id: String = UUID.randomUUID().toString(),
    val backendId: Int? = null,
    val name: String = "Sofa",
    val objectType: String = "sofa",
    val column: Int = 0,
    val row: Int = 0,
    val spanColumns: Int = 8,
    val spanRows: Int = 4,
    val rotationDegrees: Int = 0
) {
    val rotatedSpanColumns: Int get() = if (rotationDegrees % 180 == 0) spanColumns else spanRows
    val rotatedSpanRows: Int get() = if (rotationDegrees % 180 == 0) spanRows else spanColumns

    fun fitsInside(zone: GridZoneGeometry): Boolean =
        column >= 0 && row >= 0 && column + rotatedSpanColumns <= zone.spanColumns && row + rotatedSpanRows <= zone.spanRows
}

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
    val opacity: Float,
    val column: Int? = null,
    val row: Int? = null,
    val spanColumns: Int? = null,
    val spanRows: Int? = null,
    val objects: List<PlanZoneObjectDraft> = emptyList()
)

fun PlanZoneDraft.geometry(grid: FloorGridSpec): GridZoneGeometry {
    val inferredColumn = (x * grid.columns).roundToInt()
    val inferredRow = (y * grid.rows).roundToInt()
    val inferredWidth = (width * grid.columns).roundToInt().coerceAtLeast(1)
    val inferredHeight = (height * grid.rows).roundToInt().coerceAtLeast(1)
    return GridZoneGeometry(
        column = column ?: inferredColumn,
        row = row ?: inferredRow,
        spanColumns = spanColumns ?: inferredWidth,
        spanRows = spanRows ?: inferredHeight
    ).bounded(grid)
}

data class GridZoneGeometry(
    val column: Int,
    val row: Int,
    val spanColumns: Int,
    val spanRows: Int
) {
    fun bounded(grid: FloorGridSpec): GridZoneGeometry {
        val width = spanColumns.coerceIn(1, grid.columns)
        val height = spanRows.coerceIn(1, grid.rows)
        return copy(
            column = column.coerceIn(0, grid.columns - width),
            row = row.coerceIn(0, grid.rows - height),
            spanColumns = width,
            spanRows = height
        )
    }

    fun toDraftGeometry(zone: PlanZoneDraft, grid: FloorGridSpec): PlanZoneDraft {
        val bounded = bounded(grid)
        return zone.copy(
            x = bounded.column.toFloat() / grid.columns,
            y = bounded.row.toFloat() / grid.rows,
            width = bounded.spanColumns.toFloat() / grid.columns,
            height = bounded.spanRows.toFloat() / grid.rows,
            column = bounded.column,
            row = bounded.row,
            spanColumns = bounded.spanColumns,
            spanRows = bounded.spanRows
        )
    }

    val centerColumn: Float get() = column + spanColumns / 2f
    val centerRow: Float get() = row + spanRows / 2f
}

fun PlanZoneDraft.toSnapshot(): PlanZoneDraftSnapshot = PlanZoneDraftSnapshot(
    id = id, backendId = backendId, name = name, x = x, y = y, width = width, height = height,
    fillColor = fillColor, strokeColor = strokeColor, strokeWidth = strokeWidth, opacity = opacity,
    column = column, row = row, spanColumns = spanColumns, spanRows = spanRows, objects = objects
)

fun PlanZoneDraftSnapshot.toDraft(): PlanZoneDraft = PlanZoneDraft(
    id = id, backendId = backendId, name = name, x = x, y = y, width = width, height = height,
    fillColor = fillColor, strokeColor = strokeColor, strokeWidth = strokeWidth, opacity = opacity,
    column = column, row = row, spanColumns = spanColumns, spanRows = spanRows, objects = objects
)

fun String.asZoneColor(): Color = Color(android.graphics.Color.parseColor(this))

val PlanZonePalette = listOf("#76D6D0", "#F7B36E", "#8EACFF", "#F08BA2", "#85D69A", "#C68CF1", "#B8C2D8")
