package com.app.zonetask.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.zonetask.ui.screens.plan.FloorGridSpec
import com.app.zonetask.ui.screens.plan.GridZoneGeometry
import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import com.app.zonetask.ui.screens.plan.asZoneColor
import com.app.zonetask.ui.screens.plan.geometry
import com.app.zonetask.ui.theme.AppPrimary
import kotlin.math.floor
import kotlin.math.roundToInt

private data class BoardFitState(
    val scale: Float,
    val pan: Offset
)

private enum class InteractionMode {
    Idle,
    Drawing,
    MovingZone,
    ResizingZone
}

@Composable
fun EditableFloorPlanBoard(
    grid: FloorGridSpec,
    zones: List<PlanZoneDraft>,
    selectedZoneId: String?,
    isRoomToolActive: Boolean,
    focusRequestKey: Int,
    focusZoneId: String? = null,
    resetRequestKey: Int = 0,
    modifier: Modifier = Modifier,
    onZoneSelected: (String?) -> Unit,
    onRoomCreated: (column: Int, row: Int, spanColumns: Int, spanRows: Int) -> Unit,
    onZoneGeometryChanged: (id: String, column: Int, row: Int, spanColumns: Int, spanRows: Int) -> Unit,
    onZoneDelete: (String) -> Unit
) {
    val density = LocalDensity.current
    var zoom by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var fitState by remember { mutableStateOf(BoardFitState(scale = 1f, pan = Offset.Zero)) }
    var hasUserInteracted by remember { mutableStateOf(false) }
    var lastAppliedFitRequest by remember { mutableIntStateOf(Int.MIN_VALUE) }
    var interactionMode by remember { mutableStateOf(InteractionMode.Idle) }
    var drawStart by remember { mutableStateOf<GridPoint?>(null) }
    var drawCurrent by remember { mutableStateOf<GridPoint?>(null) }

    BoxWithConstraints(modifier = modifier) {
        val viewportWidth = constraints.maxWidth.toFloat()
        val viewportHeight = constraints.maxHeight.toFloat()
        val baseCellPx = 32f
        val worldWidth = grid.columns * baseCellPx
        val worldHeight = grid.rows * baseCellPx
        val origin = Offset((viewportWidth - worldWidth) / 2f, (viewportHeight - worldHeight) / 2f)
        val bottomInsetPx = 0f

        LaunchedEffect(viewportWidth, viewportHeight, worldWidth, worldHeight, bottomInsetPx, zones.hashCode(), resetRequestKey) {
            if (!hasUserInteracted || resetRequestKey != lastAppliedFitRequest) {
                val fit = zones.fitToBoard(
                    grid = grid,
                    viewportWidth = viewportWidth,
                    viewportHeight = viewportHeight,
                    bottomInsetPx = bottomInsetPx,
                    origin = origin,
                    baseCellPx = baseCellPx,
                    worldWidth = worldWidth,
                    worldHeight = worldHeight
                )
                zoom = fit.scale
                pan = fit.pan
                fitState = fit
                lastAppliedFitRequest = resetRequestKey
            }
        }
        LaunchedEffect(focusRequestKey) {
            val zone = zones.firstOrNull { it.id == focusZoneId } ?: return@LaunchedEffect
            val geometry = zone.geometry(grid)
            val targetZoom = 1.45f
            val centerX = origin.x + geometry.centerColumn * baseCellPx
            val centerY = origin.y + geometry.centerRow * baseCellPx
            zoom = targetZoom
            pan = Offset(viewportWidth / 2f - centerX * targetZoom, viewportHeight / 2f - centerY * targetZoom)
        }

        fun toGridPoint(position: Offset): GridPoint {
            val localX = (position.x - pan.x) / zoom - origin.x
            val localY = (position.y - pan.y) / zoom - origin.y
            return GridPoint(
                column = floor(localX / baseCellPx).toInt().coerceIn(0, grid.columns - 1),
                row = floor(localY / baseCellPx).toInt().coerceIn(0, grid.rows - 1)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isRoomToolActive) Modifier.pointerInput(grid, zoom, pan) {
                        if (interactionMode == InteractionMode.Idle) {
                            detectDragGestures(
                                onDragStart = { position ->
                                    hasUserInteracted = true
                                    interactionMode = InteractionMode.Drawing
                                    val point = toGridPoint(position)
                                    drawStart = point
                                    drawCurrent = point
                                },
                                onDragCancel = {
                                    interactionMode = InteractionMode.Idle
                                    drawStart = null
                                    drawCurrent = null
                                },
                                onDragEnd = {
                                    val start = drawStart
                                    val end = drawCurrent
                                    if (start != null && end != null) {
                                        onRoomCreated(
                                            minOf(start.column, end.column), minOf(start.row, end.row),
                                            kotlin.math.abs(end.column - start.column) + 1,
                                            kotlin.math.abs(end.row - start.row) + 1
                                        )
                                    }
                                    drawStart = null
                                    drawCurrent = null
                                    interactionMode = InteractionMode.Idle
                                },
                                onDrag = { change, _ ->
                                    drawCurrent = toGridPoint(change.position)
                                    change.consumeAllChanges()
                                }
                            )
                        }
                    } else Modifier
                        .pointerInput(interactionMode) {
                            if (interactionMode == InteractionMode.Idle) {
                                detectTapGestures(onTap = { onZoneSelected(null) })
                            }
                        }
                        .pointerInput(viewportWidth, viewportHeight, worldWidth, worldHeight, bottomInsetPx, interactionMode) {
                            if (interactionMode == InteractionMode.Idle) {
                                detectTransformGestures { centroid, panChange, zoomChange, _ ->
                                    hasUserInteracted = true
                                    val nextZoom = (zoom * zoomChange).coerceIn(0.65f, 3f)
                                    pan = clampPan(
                                        pan = centroid + (pan - centroid) * (nextZoom / zoom) + panChange,
                                        scale = nextZoom,
                                        contentWidth = worldWidth,
                                        contentHeight = worldHeight,
                                        viewportWidth = viewportWidth,
                                        viewportHeight = viewportHeight,
                                        sideMargin = 20f,
                                        verticalMargin = 20f
                                    )
                                    zoom = nextZoom
                                }
                            }
                        }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = pan.x
                        translationY = pan.y
                        scaleX = zoom
                        scaleY = zoom
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val major = Color(0xFF334144)
                    val minor = Color(0xFF263034)
                    for (column in 0..grid.columns) {
                        val x = origin.x + column * baseCellPx
                        drawLine(if (column % 4 == 0) major else minor, Offset(x, origin.y), Offset(x, origin.y + worldHeight), if (column % 4 == 0) 1.2f else 0.7f)
                    }
                    for (row in 0..grid.rows) {
                        val y = origin.y + row * baseCellPx
                        drawLine(if (row % 4 == 0) major else minor, Offset(origin.x, y), Offset(origin.x + worldWidth, y), if (row % 4 == 0) 1.2f else 0.7f)
                    }
                    val center = Offset(origin.x + worldWidth / 2f, origin.y + worldHeight / 2f)
                    drawLine(AppPrimary.copy(alpha = 0.62f), Offset(center.x - 11f, center.y), Offset(center.x + 11f, center.y), 1.5f)
                    drawLine(AppPrimary.copy(alpha = 0.62f), Offset(center.x, center.y - 11f), Offset(center.x, center.y + 11f), 1.5f)
                    drawCircle(AppPrimary, 3.5f, center)
                    val start = drawStart
                    val end = drawCurrent
                    if (start != null && end != null) {
                        val left = minOf(start.column, end.column)
                        val top = minOf(start.row, end.row)
                        val width = kotlin.math.abs(end.column - start.column) + 1
                        val height = kotlin.math.abs(end.row - start.row) + 1
                        drawRect(
                            color = AppPrimary.copy(alpha = 0.22f),
                            topLeft = Offset(origin.x + left * baseCellPx, origin.y + top * baseCellPx),
                            size = androidx.compose.ui.geometry.Size(width * baseCellPx, height * baseCellPx)
                        )
                        drawRect(
                            color = AppPrimary, topLeft = Offset(origin.x + left * baseCellPx, origin.y + top * baseCellPx),
                            size = androidx.compose.ui.geometry.Size(width * baseCellPx, height * baseCellPx), style = Stroke(2f)
                        )
                    }
                }

                zones.forEach { zone ->
                    val geometry = zone.geometry(grid)
                    val selected = zone.id == selectedZoneId
                    val x = origin.x + geometry.column * baseCellPx
                    val y = origin.y + geometry.row * baseCellPx
                    val zoneWidth = geometry.spanColumns * baseCellPx
                    val zoneHeight = geometry.spanRows * baseCellPx
                    val zoneColor = zone.fillColor.asZoneColor()
                    Box(
                        modifier = Modifier
                            .zIndex(if (selected) 1f else 0f)
                            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                            .size(with(density) { zoneWidth.toDp() }, with(density) { zoneHeight.toDp() })
                            .background(zoneColor.copy(alpha = zone.opacity), RectangleShape)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) AppPrimary else zoneColor.copy(alpha = 0.75f),
                                shape = RectangleShape
                            )
                            .pointerInput(zone.id, isRoomToolActive, zoom) {
                                if (!isRoomToolActive) {
                                    var dragOrigin: GridZoneGeometry? = null
                                    var dragDistance = Offset.Zero
                                    if (interactionMode == InteractionMode.Idle) {
                                        detectDragGestures(
                                            onDragStart = {
                                                hasUserInteracted = true
                                                interactionMode = InteractionMode.MovingZone
                                                dragOrigin = zone.geometry(grid)
                                                dragDistance = Offset.Zero
                                                onZoneSelected(zone.id)
                                            },
                                            onDragCancel = {
                                                interactionMode = InteractionMode.Idle
                                                dragOrigin = null
                                                dragDistance = Offset.Zero
                                            },
                                            onDragEnd = {
                                                interactionMode = InteractionMode.Idle
                                                dragOrigin = null
                                                dragDistance = Offset.Zero
                                            },
                                            onDrag = { change, delta ->
                                                change.consumeAllChanges()
                                                dragDistance += delta
                                                val base = dragOrigin ?: zone.geometry(grid)
                                                val movedColumn = base.column + (dragDistance.x / (baseCellPx * zoom)).roundToInt()
                                                val movedRow = base.row + (dragDistance.y / (baseCellPx * zoom)).roundToInt()
                                                val moved = base.moveBy(
                                                    movedColumn - base.column,
                                                    movedRow - base.row
                                                )
                                                onZoneGeometryChanged(zone.id, moved.column, moved.row, base.spanColumns, base.spanRows)
                                            }
                                        )
                                    }
                                }
                            }
                            .pointerInput(zone.id) { detectTapGestures(onTap = { onZoneSelected(zone.id) }) }
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            if (!selected && zoneWidth > 54f && zoneHeight > 34f) {
                                Text(
                                    zone.name,
                                    color = Color(0xFF071112),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.TopStart).offset(6.dp, 4.dp)
                                )
                            }
                            if (selected) {
                                var resizeOrigin by remember(zone.id) { mutableStateOf<GridZoneGeometry?>(null) }
                                DeleteBubble(
                                    modifier = Modifier.align(Alignment.TopEnd).offset(24.dp, (-24).dp),
                                    onClick = { onZoneDelete(zone.id) }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.TopCenter).offset(0.dp, (-8).dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dy = (delta.y / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeTop(dy)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.TopEnd).offset((-4).dp, (-4).dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dx = (delta.x / (baseCellPx * zoom)).roundToInt()
                                        val dy = (delta.y / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeTop(dy).resizeRight(dx)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.CenterStart).offset((-8).dp, 0.dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dx = (delta.x / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeLeft(dx)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.CenterEnd).offset(8.dp, 0.dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dx = (delta.x / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeRight(dx)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.BottomStart).offset((-8).dp, 8.dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dx = (delta.x / (baseCellPx * zoom)).roundToInt()
                                        val dy = (delta.y / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeLeft(dx).resizeBottom(dy)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.BottomCenter).offset(0.dp, 8.dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dy = (delta.y / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeBottom(dy)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                                ResizeHandle(
                                    modifier = Modifier.align(Alignment.BottomEnd).offset(8.dp, 8.dp),
                                    onDragStart = {
                                        hasUserInteracted = true
                                        interactionMode = InteractionMode.ResizingZone
                                        resizeOrigin = zone.geometry(grid)
                                    },
                                    onDrag = { delta ->
                                        val current = resizeOrigin ?: zone.geometry(grid)
                                        val dx = (delta.x / (baseCellPx * zoom)).roundToInt()
                                        val dy = (delta.y / (baseCellPx * zoom)).roundToInt()
                                        val resized = current.resizeRight(dx).resizeBottom(dy)
                                        onZoneGeometryChanged(zone.id, resized.column, resized.row, resized.spanColumns, resized.spanRows)
                                    },
                                    onDragEnd = {
                                        interactionMode = InteractionMode.Idle
                                        resizeOrigin = null
                                    }
                                )
                            }
                        }
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val major = Color(0xFF506366).copy(alpha = 0.08f)
                    val minor = Color(0xFF3B4A4C).copy(alpha = 0.05f)
                    for (column in 0..grid.columns) {
                        val x = origin.x + column * baseCellPx
                        drawLine(if (column % 4 == 0) major else minor, Offset(x, origin.y), Offset(x, origin.y + worldHeight), if (column % 4 == 0) 0.8f else 0.45f)
                    }
                    for (row in 0..grid.rows) {
                        val y = origin.y + row * baseCellPx
                        drawLine(if (row % 4 == 0) major else minor, Offset(origin.x, y), Offset(origin.x + worldWidth, y), if (row % 4 == 0) 0.8f else 0.45f)
                    }
                }
            }
        }
    }
}

private data class GridPoint(val column: Int, val row: Int)

private data class BoardBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f
}

private fun List<PlanZoneDraft>.fitToBoard(
    grid: FloorGridSpec,
    viewportWidth: Float,
    viewportHeight: Float,
    bottomInsetPx: Float,
    origin: Offset,
    baseCellPx: Float,
    worldWidth: Float,
    worldHeight: Float
): BoardFitState {
    val bounds = boundingBox(grid, origin, baseCellPx) ?: BoardBounds(origin.x, origin.y, origin.x + worldWidth, origin.y + worldHeight)
    val padding = maxOf(baseCellPx * 2.2f, minOf(bounds.width, bounds.height) * 0.14f)
    val contentWidth = (bounds.width + padding * 2f).coerceAtLeast(baseCellPx * 8f)
    val contentHeight = (bounds.height + padding * 2f).coerceAtLeast(baseCellPx * 8f)
    val visibleWidth = (viewportWidth - 24f).coerceAtLeast(1f)
    val visibleHeight = (viewportHeight - bottomInsetPx * 0.1f - 24f).coerceAtLeast(1f)
    val fitScale = minOf(visibleWidth / contentWidth, visibleHeight / contentHeight)
    val targetScale = (fitScale * 0.94f).coerceIn(0.45f, 3.6f)
    val viewportCenterX = viewportWidth / 2f
    val viewportCenterY = (viewportHeight - bottomInsetPx * 0.1f) / 2f

    return BoardFitState(
        scale = targetScale,
        pan = Offset(
            x = viewportCenterX - targetScale * bounds.centerX,
            y = viewportCenterY - targetScale * bounds.centerY
        )
    )
}

private fun List<PlanZoneDraft>.boundingBox(grid: FloorGridSpec, origin: Offset, baseCellPx: Float): BoardBounds? {
    if (isEmpty()) return null

    val geometries = map { it.geometry(grid) }
    val left = geometries.minOf { it.column }.toFloat()
    val top = geometries.minOf { it.row }.toFloat()
    val right = geometries.maxOf { it.column + it.spanColumns }.toFloat()
    val bottom = geometries.maxOf { it.row + it.spanRows }.toFloat()

    return BoardBounds(
        left = origin.x + left * baseCellPx,
        top = origin.y + top * baseCellPx,
        right = origin.x + right * baseCellPx,
        bottom = origin.y + bottom * baseCellPx
    )
}

private fun GridZoneGeometry.moveBy(deltaColumns: Int, deltaRows: Int): GridZoneGeometry =
    copy(column = column + deltaColumns, row = row + deltaRows)

private fun GridZoneGeometry.resizeLeft(deltaColumns: Int): GridZoneGeometry {
    val shift = deltaColumns.coerceIn(-(column), spanColumns - 1)
    return copy(column = column + shift, spanColumns = (spanColumns - shift).coerceAtLeast(1))
}

private fun GridZoneGeometry.resizeTop(deltaRows: Int): GridZoneGeometry {
    val shift = deltaRows.coerceIn(-(row), spanRows - 1)
    return copy(row = row + shift, spanRows = (spanRows - shift).coerceAtLeast(1))
}

private fun GridZoneGeometry.resizeRight(deltaColumns: Int): GridZoneGeometry =
    copy(spanColumns = (spanColumns + deltaColumns).coerceAtLeast(1))

private fun GridZoneGeometry.resizeBottom(deltaRows: Int): GridZoneGeometry =
    copy(spanRows = (spanRows + deltaRows).coerceAtLeast(1))

private fun clampPan(
    pan: Offset,
    scale: Float,
    contentWidth: Float,
    contentHeight: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    sideMargin: Float,
    verticalMargin: Float
): Offset {
    val scaledWidth = contentWidth * scale
    val scaledHeight = contentHeight * scale

    val clampedX = if (scaledWidth + sideMargin * 2f <= viewportWidth) {
        (viewportWidth - scaledWidth) / 2f
    } else {
        pan.x.coerceIn(viewportWidth - scaledWidth - sideMargin, sideMargin)
    }

    val clampedY = if (scaledHeight + verticalMargin * 2f <= viewportHeight) {
        (viewportHeight - scaledHeight) / 2f
    } else {
        pan.y.coerceIn(viewportHeight - scaledHeight - verticalMargin, verticalMargin)
    }

    return Offset(clampedX, clampedY)
}

@Composable
private fun DeleteBubble(modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFF4F5F5),
        shape = RoundedCornerShape(99.dp),
        border = BorderStroke(1.dp, Color(0xFFB06A74)),
        modifier = modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Close, contentDescription = null, tint = Color(0xFF8C3F4A), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ResizeHandle(
    modifier: Modifier,
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(Color(0xFFF3F4F5), RoundedCornerShape(99.dp))
            .border(1.dp, Color(0xFF91A1A6), RoundedCornerShape(99.dp))
            .pointerInput(Unit) {
                var accumulated = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        accumulated = Offset.Zero
                        onDragStart()
                    },
                    onDragCancel = {
                        accumulated = Offset.Zero
                        onDragEnd()
                    },
                    onDragEnd = {
                        accumulated = Offset.Zero
                        onDragEnd()
                    },
                    onDrag = { change, delta ->
                        change.consumeAllChanges()
                        accumulated += delta
                        onDrag(accumulated)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) { }
}
