package com.app.zonetask.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import com.app.zonetask.ui.screens.plan.asZoneColor
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import kotlin.math.roundToInt

@Composable
fun EditableFloorPlanBoard(
    worldWidth: Float,
    worldHeight: Float,
    zones: List<PlanZoneDraft>,
    selectedZoneId: String?,
    modifier: Modifier = Modifier,
    onZoneSelected: (String) -> Unit,
    onZoneDragged: (String, Float, Float) -> Unit,
    onZoneResized: (String, Float, Float) -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF0B0B0B))
    ) {
        val boardWidthPx = constraints.maxWidth.toFloat()
        val boardHeightPx = constraints.maxHeight.toFloat()
        if (boardWidthPx <= 0f || boardHeightPx <= 0f || worldWidth <= 0f || worldHeight <= 0f) {
            return@BoxWithConstraints
        }

        val fitScale = minOf(boardWidthPx / worldWidth, boardHeightPx / worldHeight)
        if (!fitScale.isFinite() || fitScale <= 0f) {
            return@BoxWithConstraints
        }

        val planPxWidth = worldWidth * fitScale
        val planPxHeight = worldHeight * fitScale
        val originX = ((boardWidthPx - planPxWidth) / 2f).coerceAtLeast(0f)
        val originY = ((boardHeightPx - planPxHeight) / 2f).coerceAtLeast(0f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF121212), Color(0xFF0A0A0A))
                )
            )
            drawRect(
                color = AppSurface,
                topLeft = Offset(originX, originY),
                size = androidx.compose.ui.geometry.Size(planPxWidth, planPxHeight)
            )

            val gridColor = Color(0xFF2A3238)
            val gridStroke = (1f / density.density).coerceAtLeast(0.65f)
            val cellSize = (50f * fitScale).coerceAtLeast(8f)
            val maxColumns = ((planPxWidth / cellSize).toInt() + 2).coerceAtMost(250)
            val maxRows = ((planPxHeight / cellSize).toInt() + 2).coerceAtMost(250)

            var x = originX
            repeat(maxColumns) { column ->
                drawLine(
                    color = gridColor.copy(alpha = if (column % 4 == 0) 0.55f else 0.28f),
                    start = Offset(x, originY),
                    end = Offset(x, originY + planPxHeight),
                    strokeWidth = gridStroke
                )
                x += cellSize
            }

            var y = originY
            repeat(maxRows) { row ->
                drawLine(
                    color = gridColor.copy(alpha = if (row % 4 == 0) 0.55f else 0.28f),
                    start = Offset(originX, y),
                    end = Offset(originX + planPxWidth, y),
                    strokeWidth = gridStroke
                )
                y += cellSize
            }

            drawRect(
                color = AppPrimary.copy(alpha = 0.55f),
                topLeft = Offset(originX, originY),
                size = androidx.compose.ui.geometry.Size(planPxWidth, planPxHeight),
                style = Stroke(width = 2.2f)
            )
        }

        if (zones.isEmpty()) {
            Text(
                text = "Agrega una zona para empezar",
                color = AppSecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        zones.forEach { zone ->
            val zoneColor = zone.fillColor.asZoneColor()
            val selected = zone.id == selectedZoneId
            val x = originX + (zone.x * planPxWidth)
            val y = originY + (zone.y * planPxHeight)
            val width = zone.width * planPxWidth
            val height = zone.height * planPxHeight
            val luminance = (0.299f * zoneColor.red) + (0.587f * zoneColor.green) + (0.114f * zoneColor.blue)
            val contentColor = if (luminance < 0.45f) Color.White else Color(0xFF101010)

            Surface(
                modifier = Modifier
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .size(
                        width = with(density) { width.toDp() },
                        height = with(density) { height.toDp() }
                    )
                    .pointerInput(zone.id) {
                        detectTapGestures(
                            onTap = { onZoneSelected(zone.id) }
                        )
                    }
                    .pointerInput(zone.id, planPxWidth, planPxHeight) {
                        detectDragGestures { change, dragAmount ->
                            change.consumeAllChanges()
                            onZoneDragged(
                                zone.id,
                                dragAmount.x / planPxWidth,
                                dragAmount.y / planPxHeight
                            )
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                color = zoneColor.copy(alpha = zone.opacity),
                border = BorderStroke(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) AppPrimary else zoneColor.copy(alpha = 0.45f)
                ),
                shadowElevation = if (selected) 8.dp else 2.dp,
                tonalElevation = 0.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                color = Color.Black.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(bottomEnd = 14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = zone.name,
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "${(zone.width * 100).roundToInt()}% x ${(zone.height * 100).roundToInt()}%",
                        color = contentColor.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 10.dp)
                    )

                    if (selected) {
                        ResizeHandle(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            onDrag = { deltaX, deltaY ->
                                onZoneResized(
                                    zone.id,
                                    deltaX / planPxWidth,
                                    deltaY / planPxHeight
                                )
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .background(
                    color = Color(0xFF0F1113).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Toca, arrastra y redimensiona las zonas",
                color = AppSecondaryText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ResizeHandle(
    modifier: Modifier = Modifier,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = modifier
            .padding(10.dp)
            .size(26.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(AppPrimary)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "↘",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
