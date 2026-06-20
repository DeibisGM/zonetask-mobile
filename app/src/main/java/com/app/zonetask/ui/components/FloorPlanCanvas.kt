package com.app.zonetask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.zonetask.ui.screens.plan.FloorGridSpec
import com.app.zonetask.ui.screens.plan.GridZoneGeometry
import com.app.zonetask.ui.theme.AppPrimary
import kotlin.math.abs
import kotlin.math.roundToInt

data class FloorPlanZonePreview(
    val id: String,
    val backendId: Int? = null,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val fillColor: String,
    val strokeColor: String = "#0F0F0F",
    val strokeWidth: Float = 2f,
    val opacity: Float = 0.9f,
    val shapeType: String = "rectangle",
    val taskCount: Int = 0
)

internal data class HomeBoardFitState(
    val scale: Float,
    val pan: Offset
)

internal data class HomeBoardBounds(
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

private const val SCALE_MIN = 0.4f
private const val SCALE_MAX = 14f
private const val BASE_CELL_PX = 32f

@Composable
fun FloorPlanCanvas(
    gridColumns: Int,
    gridRows: Int,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    zones: List<FloorPlanZonePreview> = emptyList(),
    selectedZoneBackendId: Int? = null,
    onZoneClick: (FloorPlanZonePreview) -> Unit = {},
    onBackgroundTap: () -> Unit = {}
) {
    val density = LocalDensity.current
    val columns = gridColumns.coerceAtLeast(1)
    val rows = gridRows.coerceAtLeast(1)
    val grid = remember(columns, rows) { FloorGridSpec(columns, rows) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090B0C))
    ) {
        val canvasW = constraints.maxWidth.toFloat()
        val canvasH = constraints.maxHeight.toFloat()
        val bottomInsetPx = with(density) { bottomInset.toPx() }
        val bottomReservePx = (bottomInsetPx * 0.62f).coerceAtLeast(0f)
        val totalBottomReservePx = (bottomReservePx + bottomInsetPx * 0.1f).coerceAtLeast(bottomReservePx)
        val availableHeight = (canvasH - totalBottomReservePx).coerceAtLeast(1f)
        val worldWidth = grid.columns * BASE_CELL_PX
        val worldHeight = grid.rows * BASE_CELL_PX
        val origin = Offset((canvasW - worldWidth) / 2f, (canvasH - worldHeight) / 2f)
        val boardBounds = HomeBoardBounds(
            left = origin.x,
            top = origin.y,
            right = origin.x + worldWidth,
            bottom = origin.y + worldHeight
        )
        val contentBounds = zones.boundingBox(grid, origin) ?: HomeBoardBounds(
            left = origin.x,
            top = origin.y,
            right = origin.x + worldWidth,
            bottom = origin.y + worldHeight
        )

        var scale by remember { mutableFloatStateOf(1f) }
        var pan by remember { mutableStateOf(Offset.Zero) }
        var fitState by remember { mutableStateOf(HomeBoardFitState(scale = 1f, pan = Offset.Zero)) }

        LaunchedEffect(canvasW, canvasH, grid.columns, grid.rows, bottomInsetPx, zones.hashCode()) {
            val fit = zones.fitToBoard(
                grid = grid,
                viewportWidth = canvasW,
                viewportHeight = canvasH,
                bottomReservePx = totalBottomReservePx,
                origin = origin,
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                bounds = contentBounds
            )
            scale = fit.scale
            pan = fit.pan
            fitState = fit
        }

        val isOffCenter by remember(scale, pan, fitState) {
            derivedStateOf {
                abs(pan.x - fitState.pan.x) > 20f || abs(pan.y - fitState.pan.y) > 20f || abs(scale - fitState.scale) > 0.1f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(canvasW, availableHeight, boardBounds) {
                    detectTransformGestures { centroid, panChange, zoomChange, _ ->
                        val previousScale = scale
                        val nextScale = (previousScale * zoomChange).coerceIn(SCALE_MIN, SCALE_MAX)
                        val scaleRatio = if (previousScale == 0f) 1f else nextScale / previousScale
                        val nextPan = centroid + (pan - centroid) * scaleRatio + panChange

                        pan = clampPan(
                            pan = nextPan,
                            scale = nextScale,
                            contentBounds = boardBounds,
                            viewportWidth = canvasW,
                            viewportHeight = availableHeight,
                            sideMargin = 96f,
                            verticalMargin = 96f
                        )
                        scale = nextScale
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            // Pointer input is reported in viewport coordinates while the board is
                            // rendered with a graphics transform. Convert it back before hit-testing.
                            val boardTap = tapOffset.toBoardPoint(scale, pan)
                            val tappedZone = zones.asReversed().firstOrNull { zone ->
                                zone.containsPoint(boardTap, grid, origin)
                            }

                            if (tappedZone != null) {
                                if (tappedZone.backendId != null) {
                                    onZoneClick(tappedZone)
                                } else {
                                    onBackgroundTap()
                                }
                            } else {
                                onBackgroundTap()
                            }
                        },
                        onDoubleTap = {
                            scale = fitState.scale
                            pan = fitState.pan
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = pan.x
                    translationY = pan.y
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            zones.forEach { zone ->
                val geometry = zone.geometry(grid)
                val zoneX = origin.x + geometry.column * BASE_CELL_PX
                val zoneY = origin.y + geometry.row * BASE_CELL_PX
                val zoneWidth = geometry.spanColumns * BASE_CELL_PX
                val zoneHeight = geometry.spanRows * BASE_CELL_PX
                val baseColor = zone.fillColor.asComposeColor()
                val hasPendingTasks = zone.taskCount > 0
                val selected = zone.backendId != null && zone.backendId == selectedZoneBackendId
                val fillColor = baseColor.copy(
                    alpha = when {
                        selected -> zone.opacity.coerceIn(0f, 1f)
                        hasPendingTasks -> (zone.opacity * 0.96f).coerceIn(0f, 1f)
                        else -> (zone.opacity * 0.94f).coerceIn(0f, 1f)
                    }
                )
                val borderColor = when {
                    selected -> AppPrimary
                    hasPendingTasks -> baseColor.copy(alpha = 0.92f)
                    else -> baseColor.copy(alpha = 0.68f)
                }

                Box(
                    modifier = Modifier
                        .zIndex(if (selected) 2f else if (hasPendingTasks) 1f else 0f)
                        .offset { IntOffset(zoneX.roundToInt(), zoneY.roundToInt()) }
                        .size(
                            with(density) { zoneWidth.toDp() },
                            with(density) { zoneHeight.toDp() }
                        )
                        .background(fillColor, RectangleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = borderColor,
                            shape = RectangleShape
                        )
                ) {
                    if (zoneWidth > 54f && zoneHeight > 34f) {
                        Text(
                            text = zone.name,
                            color = if (selected) Color.White else Color(0xFF071112),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 5.dp, top = 3.dp)
                        )
                    }

                    if (hasPendingTasks) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(5.dp)
                                .size(7.dp)
                                .background(AppPrimary, androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                }
            }

            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawGridOverlay(grid, origin, worldWidth, worldHeight)
            }
        }

        AnimatedVisibility(
            visible = isOffCenter,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = {
                    scale = fitState.scale
                    pan = fitState.pan
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary.copy(alpha = 0.9f)),
                modifier = Modifier.padding(bottom = bottomInset)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(" Center", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun DrawScope.drawGrid(
    grid: FloorGridSpec,
    origin: Offset,
    worldWidth: Float,
    worldHeight: Float
) {
    val major = Color(0xFF334144)
    val minor = Color(0xFF263034)

    for (column in 0..grid.columns) {
        val x = origin.x + column * BASE_CELL_PX
        val isMajor = column % 4 == 0
        drawLine(
            color = if (isMajor) major else minor,
            start = Offset(x, origin.y),
            end = Offset(x, origin.y + worldHeight),
            strokeWidth = if (isMajor) 1.2f else 0.7f
        )
    }

    for (row in 0..grid.rows) {
        val y = origin.y + row * BASE_CELL_PX
        val isMajor = row % 4 == 0
        drawLine(
            color = if (isMajor) major else minor,
            start = Offset(origin.x, y),
            end = Offset(origin.x + worldWidth, y),
            strokeWidth = if (isMajor) 1.2f else 0.7f
        )
    }

    val center = Offset(origin.x + worldWidth / 2f, origin.y + worldHeight / 2f)
    drawLine(AppPrimary.copy(alpha = 0.62f), Offset(center.x - 11f, center.y), Offset(center.x + 11f, center.y), 1.5f)
    drawLine(AppPrimary.copy(alpha = 0.62f), Offset(center.x, center.y - 11f), Offset(center.x, center.y + 11f), 1.5f)
    drawCircle(AppPrimary, 3.5f, center)
}

private fun DrawScope.drawGridOverlay(
    grid: FloorGridSpec,
    origin: Offset,
    worldWidth: Float,
    worldHeight: Float
) {
    val major = Color(0xFF86A1A5).copy(alpha = 0.16f)
    val minor = Color(0xFF708789).copy(alpha = 0.11f)

    for (column in 0..grid.columns) {
        val x = origin.x + column * BASE_CELL_PX
        val isMajor = column % 4 == 0
        drawLine(
            color = if (isMajor) major else minor,
            start = Offset(x, origin.y),
            end = Offset(x, origin.y + worldHeight),
            strokeWidth = if (isMajor) 1.05f else 0.72f
        )
    }

    for (row in 0..grid.rows) {
        val y = origin.y + row * BASE_CELL_PX
        val isMajor = row % 4 == 0
        drawLine(
            color = if (isMajor) major else minor,
            start = Offset(origin.x, y),
            end = Offset(origin.x + worldWidth, y),
            strokeWidth = if (isMajor) 1.05f else 0.72f
        )
    }
}

private fun String.asComposeColor(alpha: Float = 1f): Color {
    return Color(android.graphics.Color.parseColor(this)).copy(alpha = alpha.coerceIn(0f, 1f))
}

private fun FloorPlanZonePreview.geometry(grid: FloorGridSpec): GridZoneGeometry {
    val inferredColumn = (x * grid.columns).roundToInt()
    val inferredRow = (y * grid.rows).roundToInt()
    val inferredWidth = (width * grid.columns).roundToInt().coerceAtLeast(1)
    val inferredHeight = (height * grid.rows).roundToInt().coerceAtLeast(1)
    return GridZoneGeometry(
        column = inferredColumn,
        row = inferredRow,
        spanColumns = inferredWidth,
        spanRows = inferredHeight
    ).bounded(grid)
}

private fun List<FloorPlanZonePreview>.fitToBoard(
    grid: FloorGridSpec,
    viewportWidth: Float,
    viewportHeight: Float,
    bottomReservePx: Float,
    origin: Offset,
    worldWidth: Float,
    worldHeight: Float,
    bounds: HomeBoardBounds
): HomeBoardFitState {
    val padding = maxOf(BASE_CELL_PX * 0.9f, minOf(bounds.width, bounds.height) * 0.055f)
    val contentWidth = (bounds.width + padding * 2f).coerceAtLeast(BASE_CELL_PX * 4f)
    val contentHeight = (bounds.height + padding * 2f).coerceAtLeast(BASE_CELL_PX * 4f)
    val visibleWidth = (viewportWidth - 12f).coerceAtLeast(1f)
    val visibleHeight = (viewportHeight - bottomReservePx - 12f).coerceAtLeast(1f)
    val fitScale = minOf(visibleWidth / contentWidth, visibleHeight / contentHeight)
    val targetScale = fitScale.coerceIn(SCALE_MIN, SCALE_MAX)
    val viewportCenterX = viewportWidth / 2f
    val viewportCenterY = (viewportHeight - bottomReservePx) / 2f

    return HomeBoardFitState(
        scale = targetScale,
        pan = Offset(
            x = viewportCenterX - targetScale * bounds.centerX,
            y = viewportCenterY - targetScale * bounds.centerY
        )
    )
}

private fun FloorPlanZonePreview.containsPoint(
    tap: Offset,
    grid: FloorGridSpec,
    origin: Offset
): Boolean {
    val geometry = geometry(grid)
    val left = origin.x + geometry.column * BASE_CELL_PX
    val top = origin.y + geometry.row * BASE_CELL_PX
    val right = left + geometry.spanColumns * BASE_CELL_PX
    val bottom = top + geometry.spanRows * BASE_CELL_PX
    return tap.x in left..right && tap.y in top..bottom
}

private fun Offset.toBoardPoint(scale: Float, pan: Offset): Offset {
    val safeScale = scale.coerceAtLeast(0.0001f)
    return Offset(
        x = (x - pan.x) / safeScale,
        y = (y - pan.y) / safeScale
    )
}

private fun List<FloorPlanZonePreview>.boundingBox(
    grid: FloorGridSpec,
    origin: Offset
): HomeBoardBounds? {
    if (isEmpty()) return null

    val geometries = map { it.geometry(grid) }
    val left = geometries.minOf { it.column }.toFloat()
    val top = geometries.minOf { it.row }.toFloat()
    val right = geometries.maxOf { it.column + it.spanColumns }.toFloat()
    val bottom = geometries.maxOf { it.row + it.spanRows }.toFloat()

    return HomeBoardBounds(
        left = origin.x + left * BASE_CELL_PX,
        top = origin.y + top * BASE_CELL_PX,
        right = origin.x + right * BASE_CELL_PX,
        bottom = origin.y + bottom * BASE_CELL_PX
    )
}

private fun clampPan(
    pan: Offset,
    scale: Float,
    contentBounds: HomeBoardBounds,
    viewportWidth: Float,
    viewportHeight: Float,
    sideMargin: Float,
    verticalMargin: Float
): Offset {
    val minX = viewportWidth - sideMargin - scale * contentBounds.right
    val maxX = sideMargin - scale * contentBounds.left
    val minY = viewportHeight - verticalMargin - scale * contentBounds.bottom
    val maxY = verticalMargin - scale * contentBounds.top

    val clampedX = pan.x.coerceIn(minOf(minX, maxX), maxOf(minX, maxX))
    val clampedY = pan.y.coerceIn(minOf(minY, maxY), maxOf(minY, maxY))

    return Offset(clampedX, clampedY)
}
