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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSurface
import kotlin.math.abs

private const val SCALE_MIN  = 0.4f
private const val SCALE_MAX  = 6f
private const val GRID_CELL  = 50f
private const val GRID_CELL_MIN_PX = 20f

@Composable
fun FloorPlanCanvas(
    worldWidth:  Float,
    worldHeight: Float,
    modifier:    Modifier = Modifier,
    bottomInset: Dp = 0.dp
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        val canvasW = constraints.maxWidth.toFloat()
        val canvasH = constraints.maxHeight.toFloat()

        val worldToCanvas = minOf(canvasW / worldWidth, canvasH / worldHeight)

        val initDx = (canvasW - worldWidth  * worldToCanvas) / 2f
        val initDy = (canvasH - worldHeight * worldToCanvas) / 2f

        val planW = worldWidth  * worldToCanvas
        val planH = worldHeight * worldToCanvas

        var scale  by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val isOffCenter by remember {
            derivedStateOf {
                abs(offset.x) > 20f || abs(offset.y) > 20f || abs(scale - 1f) > 0.1f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(SCALE_MIN, SCALE_MAX)
                        offset += pan
                        // Clamp so plan stays at least partially visible
                        val scaledW = planW * scale
                        val scaledH = planH * scale
                        val margin = 100f
                        offset = Offset(
                            x = offset.x.coerceIn(
                                -scaledW + margin - initDx * scale,
                                canvasW - margin - initDx * scale
                            ),
                            y = offset.y.coerceIn(
                                -scaledH + margin - initDy * scale,
                                canvasH - margin - initDy * scale
                            )
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        scale  = 1f
                        offset = Offset.Zero
                    })
                }
                .graphicsLayer {
                    scaleX       = scale
                    scaleY       = scale
                    translationX = offset.x + initDx * scale
                    translationY = offset.y + initDy * scale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = AppSurface, topLeft = Offset.Zero, size = Size(planW, planH))
                drawGrid(planW, planH, worldToCanvas, scale)
                drawRect(
                    color = AppPrimary.copy(alpha = 0.7f),
                    topLeft = Offset.Zero,
                    size = Size(planW, planH),
                    style = Stroke(width = 2.dp.toPx())
                )
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
                onClick = { scale = 1f; offset = Offset.Zero },
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
    val gridColor = Color(0xFF2A2A2A)
    var x = cellPx
    while (x < planW) { drawLine(gridColor, Offset(x, 0f), Offset(x, planH), stroke); x += cellPx }
    var y = cellPx
    while (y < planH) { drawLine(gridColor, Offset(0f, y), Offset(planW, y), stroke); y += cellPx }
}
