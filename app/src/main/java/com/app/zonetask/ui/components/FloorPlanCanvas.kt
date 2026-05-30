package com.app.zonetask.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSurface

private const val SCALE_MIN  = 0.4f
private const val SCALE_MAX  = 6f
// Grid cell in world-units; at worldToCanvas=1 this is 50 logical px per tile.
private const val GRID_CELL  = 50f

/**
 * Reusable blank floor-plan canvas.
 *
 * Renders a fit-to-screen gridded plan boundary with pan/zoom gestures.
 * Double-tap resets to the initial fit view.
 * Zones (story #29) will be drawn on top of this canvas later.
 *
 * @param worldWidth  Logical width of the plan in world-units.
 * @param worldHeight Logical height of the plan in world-units.
 */
@Composable
fun FloorPlanCanvas(
    worldWidth:  Float,
    worldHeight: Float,
    modifier:    Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        val canvasW = constraints.maxWidth.toFloat()
        val canvasH = constraints.maxHeight.toFloat()

        // Scale factor so the whole plan fits on screen initially, preserving aspect ratio.
        val worldToCanvas = minOf(
            canvasW / worldWidth,
            canvasH / worldHeight
        )

        // Centre the plan when it doesn't fill the available space.
        val initDx = (canvasW - worldWidth  * worldToCanvas) / 2f
        val initDy = (canvasH - worldHeight * worldToCanvas) / 2f

        var scale  by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale  = (scale * zoom).coerceIn(SCALE_MIN, SCALE_MAX)
                        offset += pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        scale  = 1f
                        offset = Offset.Zero
                    })
                }
                // graphicsLayer lambda avoids recomposition on every pan/zoom tick (spike §1.8).
                .graphicsLayer {
                    scaleX       = scale
                    scaleY       = scale
                    translationX = offset.x + initDx * scale
                    translationY = offset.y + initDy * scale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val planW = worldWidth  * worldToCanvas
                val planH = worldHeight * worldToCanvas

                // 1. Plan background
                drawRect(
                    color   = AppSurface,
                    topLeft = Offset.Zero,
                    size    = Size(planW, planH)
                )

                // 2. Grid lines — strokeWidth divided by scale keeps them constant under zoom (spike §1.9).
                drawGrid(planW, planH, worldToCanvas, scale)

                // 3. Plan border in accent colour
                drawRect(
                    color   = AppPrimary.copy(alpha = 0.7f),
                    topLeft = Offset.Zero,
                    size    = Size(planW, planH),
                    style   = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(
    planW:        Float,
    planH:        Float,
    worldToCanvas: Float,
    scale:        Float
) {
    val cellPx    = GRID_CELL * worldToCanvas
    val stroke    = (1.dp.toPx() / scale).coerceAtLeast(0.3f)
    val gridColor = Color(0xFF2A2A2A)

    var x = cellPx
    while (x < planW) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, planH), stroke)
        x += cellPx
    }
    var y = cellPx
    while (y < planH) {
        drawLine(gridColor, Offset(0f, y), Offset(planW, y), stroke)
        y += cellPx
    }
}
