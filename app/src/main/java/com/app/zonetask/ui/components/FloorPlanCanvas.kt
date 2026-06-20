package com.app.zonetask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private data class CanvasFitState(
    val scale: Float,
    val pan: Offset
)

private data class ZoneBounds(
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

private const val SCALE_MIN  = 0.4f
private const val SCALE_MAX  = 14f
private const val GRID_CELL  = 50f
private const val GRID_CELL_MIN_PX = 20f

@Composable
fun FloorPlanCanvas(
    worldWidth:  Float,
    worldHeight: Float,
    modifier:    Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    zones: List<FloorPlanZonePreview> = emptyList()
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        val canvasW = constraints.maxWidth.toFloat()
        val canvasH = constraints.maxHeight.toFloat()
        val bottomInsetPx = with(density) { bottomInset.toPx() }

        val worldToCanvas = minOf(canvasW / worldWidth, canvasH / worldHeight)

        val planW = worldWidth  * worldToCanvas
        val planH = worldHeight * worldToCanvas

        var scale by remember { mutableFloatStateOf(1f) }
        var pan by remember { mutableStateOf(Offset.Zero) }
        var fitState by remember { mutableStateOf(CanvasFitState(scale = 1f, pan = Offset.Zero)) }

        LaunchedEffect(canvasW, canvasH, worldWidth, worldHeight, bottomInsetPx, zones.hashCode()) {
            val fit = zones.fitToCanvas(
                canvasWidth = canvasW,
                canvasHeight = canvasH,
                bottomInsetPx = bottomInsetPx,
                planWidth = planW,
                planHeight = planH,
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
                .pointerInput(canvasW, canvasH, planW, planH, bottomInsetPx) {
                    detectTransformGestures { centroid, panChange, zoomChange, _ ->
                        val previousScale = scale
                        val nextScale = (scale * zoomChange).coerceIn(SCALE_MIN, SCALE_MAX)
                        val scaleRatio = if (previousScale == 0f) 1f else nextScale / previousScale

                        pan = clampPan(
                            pan = centroid + (pan - centroid) * scaleRatio + panChange,
                            scale = nextScale,
                            contentWidth = planW,
                            contentHeight = planH,
                            viewportWidth = canvasW,
                            viewportHeight = canvasH - bottomInsetPx * 0.35f,
                            sideMargin = 22f,
                            verticalMargin = 22f
                        )
                        scale = nextScale
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        scale = fitState.scale
                        pan = fitState.pan
                    })
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = pan.x
                    translationY = pan.y
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawGrid(planW, planH, worldToCanvas, scale)
                drawZones(zones, planW, planH, scale)
            }
        }

        // Center button
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = bottomInset)
            ) {
                Icon(Icons.Filled.MyLocation, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(" Center", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun DrawScope.drawGrid(
    planW: Float, planH: Float, worldToCanvas: Float, scale: Float
) {
    var cellPx = GRID_CELL * worldToCanvas
    if (cellPx < GRID_CELL_MIN_PX) {
        val factor = (GRID_CELL_MIN_PX / cellPx).toInt().coerceAtLeast(1)
        cellPx *= factor
    }
    val stroke = (1.dp.toPx() / scale).coerceAtLeast(0.3f)
    val majorColor = Color(0xFF63787B).copy(alpha = 0.20f)
    val minorColor = Color(0xFF445356).copy(alpha = 0.12f)
    val gridMargin = maxOf(planW, planH) * 0.45f
    val startX = -gridMargin
    val endX = planW + gridMargin
    val startY = -gridMargin
    val endY = planH + gridMargin

    var x = (startX / cellPx).toInt() * cellPx
    while (x <= endX) {
        drawLine(
            if (((x / cellPx).roundToInt()) % 4 == 0) majorColor else minorColor,
            Offset(x, startY),
            Offset(x, endY),
            if (((x / cellPx).roundToInt()) % 4 == 0) stroke else (stroke * 0.7f)
        )
        x += cellPx
    }
    var y = (startY / cellPx).toInt() * cellPx
    while (y <= endY) {
        drawLine(
            if (((y / cellPx).roundToInt()) % 4 == 0) majorColor else minorColor,
            Offset(startX, y),
            Offset(endX, y),
            if (((y / cellPx).roundToInt()) % 4 == 0) stroke else (stroke * 0.7f)
        )
        y += cellPx
    }

    val center = Offset(planW / 2f, planH / 2f)
    drawCircle(Color.White.copy(alpha = 0.02f), radius = 14f, center = center)
    drawLine(Color.White.copy(alpha = 0.04f), Offset(center.x - 12f, center.y), Offset(center.x + 12f, center.y), stroke)
    drawLine(Color.White.copy(alpha = 0.04f), Offset(center.x, center.y - 12f), Offset(center.x, center.y + 12f), stroke)
}

private fun DrawScope.drawZones(
    zones: List<FloorPlanZonePreview>,
    planW: Float,
    planH: Float,
    scale: Float
) {
    if (zones.isEmpty()) return

    val zoneLabelTextSizePx = (12.sp.toPx() / scale).coerceAtLeast(10f)
    val labelPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        this.textSize = zoneLabelTextSizePx
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val chipPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(200, 5, 5, 5)
        style = android.graphics.Paint.Style.FILL
    }
    val subtleTextPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(200, 255, 255, 255)
        this.textSize = (10.sp.toPx() / scale).coerceAtLeast(8f)
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    }

    zones.forEach { zone ->
        val left = zone.x.coerceIn(0f, 1f) * planW
        val top = zone.y.coerceIn(0f, 1f) * planH
        val width = zone.width.coerceIn(0.01f, 1f) * planW
        val height = zone.height.coerceIn(0.01f, 1f) * planH
        val right = (left + width).coerceAtMost(planW)
        val bottom = (top + height).coerceAtMost(planH)
        val zoneColor = zone.fillColor.asComposeColor((zone.opacity * 0.76f).coerceIn(0f, 1f))
        val strokeColor = zone.strokeColor.asComposeColor(0.18f)
        val strokeWidth = (zone.strokeWidth / scale).coerceAtLeast(0.6f)

        when (zone.shapeType.lowercase()) {
            "ellipse" -> {
                drawOval(
                    color = zoneColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top)
                )
                drawOval(
                    color = strokeColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = strokeWidth)
                )
            }

            else -> {
                drawRect(
                    color = zoneColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top)
                )
            }
        }

        if (right - left >= 80f && bottom - top >= 44f) {
            val canvas = drawContext.canvas.nativeCanvas
            val padding = 10f / scale
            val chipHeight = (zoneLabelTextSizePx * 1.8f).coerceAtLeast(24f)
            val chipRight = right - padding
            val chipLeft = left + padding
            val chipTop = top + padding
            val chipBottom = (chipTop + chipHeight).coerceAtMost(bottom - padding)

            canvas.drawRoundRect(
                chipLeft,
                chipTop,
                chipRight,
                chipBottom,
                12f,
                12f,
                chipPaint
            )
            canvas.drawText(
                zone.name,
                chipLeft + 10f,
                chipBottom - 10f,
                labelPaint
            )

            if (zone.taskCount > 0) {
                canvas.drawText(
                    "${zone.taskCount} tareas",
                    chipLeft + 10f,
                    chipBottom + subtleTextPaint.textSize + 2f,
                    subtleTextPaint
                )
            }
        }
    }
}

private fun String.asComposeColor(alpha: Float = 1f): Color {
    return Color(android.graphics.Color.parseColor(this)).copy(alpha = alpha.coerceIn(0f, 1f))
}

private fun List<FloorPlanZonePreview>.fitToCanvas(
    canvasWidth: Float,
    canvasHeight: Float,
    bottomInsetPx: Float,
    planWidth: Float,
    planHeight: Float
): CanvasFitState {
    val bounds = boundingBox(planWidth, planHeight) ?: ZoneBounds(0f, 0f, planWidth, planHeight)
    val borderPadding = maxOf(24f, minOf(bounds.width, bounds.height) * 0.08f)
    val contentWidth = (bounds.width + borderPadding * 2f).coerceAtLeast(planWidth * 0.14f)
    val contentHeight = (bounds.height + borderPadding * 2f).coerceAtLeast(planHeight * 0.14f)
    val visibleWidth = (canvasWidth - 24f).coerceAtLeast(1f)
    val visibleHeight = (canvasHeight - bottomInsetPx * 0.35f - 24f).coerceAtLeast(1f)
    val fitScale = minOf(visibleWidth / contentWidth, visibleHeight / contentHeight)
    val targetScale = (fitScale * 1.32f).coerceIn(SCALE_MIN, SCALE_MAX)
    val viewportCenterX = canvasWidth / 2f
    val viewportCenterY = (canvasHeight - bottomInsetPx * 0.35f) / 2f

    return CanvasFitState(
        scale = targetScale,
        pan = Offset(
            x = viewportCenterX - targetScale * bounds.centerX,
            y = viewportCenterY - targetScale * bounds.centerY
        )
    )
}

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

private fun List<FloorPlanZonePreview>.boundingBox(
    planWidth: Float,
    planHeight: Float
): ZoneBounds? {
    if (isEmpty()) return null

    val left = map { it.x.coerceIn(0f, 1f) * planWidth }.minOrNull() ?: 0f
    val top = map { it.y.coerceIn(0f, 1f) * planHeight }.minOrNull() ?: 0f
    val right = map { (it.x.coerceIn(0f, 1f) + it.width.coerceIn(0.01f, 1f)) * planWidth }.maxOrNull() ?: planWidth
    val bottom = map { (it.y.coerceIn(0f, 1f) + it.height.coerceIn(0.01f, 1f)) * planHeight }.maxOrNull() ?: planHeight

    return ZoneBounds(left, top, right, bottom)
}
