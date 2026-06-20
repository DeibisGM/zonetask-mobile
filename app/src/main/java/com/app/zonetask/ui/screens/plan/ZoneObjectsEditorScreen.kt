package com.app.zonetask.ui.screens.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import kotlin.math.roundToInt

@Composable
fun ZoneObjectsEditorScreen(
    zone: PlanZoneDraft,
    grid: FloorGridSpec,
    onAddObject: (ZoneObjectCatalogItem) -> Unit,
    onMoveObject: (String, Int, Int) -> Unit,
    onRotateObject: (String) -> Unit,
    onDeleteObject: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val geometry = zone.geometry(grid)
    val largeColumns = (geometry.spanColumns / SUBCELLS_PER_METER).coerceAtLeast(1)
    val largeRows = (geometry.spanRows / SUBCELLS_PER_METER).coerceAtLeast(1)
    val context = LocalContext.current
    val svgImageLoader = remember(context) {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
    }

    Column(modifier.fillMaxSize().background(Color(0xFF090B0C))) {
        Surface(color = Color(0xFF111718), border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Objects in ${zone.name}", color = AppOnSurface, fontWeight = FontWeight.Bold)
                Text("Each square is 1 m. Objects snap to the grid and must remain inside the room.", color = AppSecondaryText, fontSize = 12.sp)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val meterCell = minOf(constraints.maxWidth.toFloat() / largeColumns, constraints.maxHeight.toFloat() / largeRows).coerceAtLeast(1f)
                val boardWidth = largeColumns * meterCell
                val boardHeight = largeRows * meterCell
                val density = LocalDensity.current
                Box(
                    Modifier
                        .size(with(density) { boardWidth.toDp() }, with(density) { boardHeight.toDp() })
                        .background(zone.fillColor.asZoneColor())
                        .border(2.dp, AppPrimary)
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        for (column in 0..largeColumns) drawLine(Color.Black.copy(alpha = .22f), Offset(column * meterCell, 0f), Offset(column * meterCell, boardHeight), 1.4f)
                        for (row in 0..largeRows) drawLine(Color.Black.copy(alpha = .22f), Offset(0f, row * meterCell), Offset(boardWidth, row * meterCell), 1.4f)
                    }
                    zone.objects.forEach { item ->
                        val latestMove by rememberUpdatedState(onMoveObject)
                        Box(
                            Modifier
                                .offset { IntOffset((item.column * meterCell / SUBCELLS_PER_METER).roundToInt(), (item.row * meterCell / SUBCELLS_PER_METER).roundToInt()) }
                                .size(
                                    with(density) { (item.rotatedSpanColumns * meterCell / SUBCELLS_PER_METER).toDp() },
                                    with(density) { (item.rotatedSpanRows * meterCell / SUBCELLS_PER_METER).toDp() }
                                )
                                .pointerInput(item.id, meterCell) {
                                    var origin = Offset.Zero
                                    detectDragGestures(
                                        onDragStart = { origin = Offset(item.column.toFloat(), item.row.toFloat()) },
                                        onDrag = { change, drag ->
                                            change.consume()
                                            origin += drag / meterCell * SUBCELLS_PER_METER.toFloat()
                                            latestMove(item.id, origin.x.roundToInt(), origin.y.roundToInt())
                                        }
                                    )
                                }
                        ) {
                            // This loads the exact SVG supplied for the object, not a drawn substitute.
                            AsyncImage(
                                model = "file:///android_asset/zone-objects/${item.assetFileName()}",
                                imageLoader = svgImageLoader,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(
                                        with(density) { (item.spanColumns * meterCell / SUBCELLS_PER_METER).toDp() },
                                        with(density) { (item.spanRows * meterCell / SUBCELLS_PER_METER).toDp() }
                                    )
                                    .align(Alignment.Center)
                                    .graphicsLayer { rotationZ = item.rotationDegrees.toFloat() }
                            )
                            Surface(onClick = { onRotateObject(item.id) }, color = Color(0xFF101718), shape = RoundedCornerShape(6.dp), modifier = Modifier.align(Alignment.TopEnd).padding(3.dp)) {
                                Text("90°", color = AppOnSurface, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            Surface(onClick = { onDeleteObject(item.id) }, color = Color(0xFF5A252A), shape = RoundedCornerShape(6.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp)) {
                                Text("Delete", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
        Surface(color = Color(0xFF111718), border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ZoneObjectCatalog.take(2).forEach { item -> LibraryButton(item, Modifier.weight(1f), onAddObject) }
                    }
                    LibraryButton(ZoneObjectCatalog[2], Modifier.fillMaxWidth(), onAddObject)
                }
                Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF273437)), modifier = Modifier.height(52.dp)) {
                    Text("Done", color = AppOnSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LibraryButton(item: ZoneObjectCatalogItem, modifier: Modifier, onAddObject: (ZoneObjectCatalogItem) -> Unit) {
    Button(onClick = { onAddObject(item) }, colors = ButtonDefaults.buttonColors(containerColor = AppPrimary), modifier = modifier.height(36.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) {
        Text(item.name, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

private fun PlanZoneObjectDraft.assetFileName(): String = when (objectType) {
    "bed" -> "bed_1x2.svg"
    "table_with_chairs" -> "table_with_chairs_2x2.svg"
    else -> "sofa_2x1.svg"
}
