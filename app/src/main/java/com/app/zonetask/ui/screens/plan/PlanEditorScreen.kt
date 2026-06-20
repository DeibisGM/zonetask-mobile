package com.app.zonetask.ui.screens.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.EditableFloorPlanBoard
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import kotlin.math.roundToInt

@Composable
fun PlanEditorScreen(
    spaceId: Int,
    planId: Int? = null,
    templateId: Int? = null,
    modifier: Modifier = Modifier,
    onSaved: (message: String) -> Unit = {},
    onBack: () -> Unit = {},
    onSaveActionChanged: ((() -> Unit)?, Boolean) -> Unit = { _, _ -> },
    viewModel: PlanEditorViewModel = viewModel(
        factory = PlanEditorViewModelFactory(AppContainer.floorPlanRepository, AppContainer.zoneRepository, AppContainer.floorPlanTemplateRepository, spaceId, planId, templateId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.isLoadingTemplate) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppPrimary)
        }
        return
    }
    if (!state.setupComplete) {
        FloorSetupScreen(state, { name -> viewModel.completeSetup(name, state.canvasWidth, state.canvasHeight) }, modifier)
        return
    }

    val grid = FloorGridSpec(state.canvasWidth.toIntOrNull() ?: 240, state.canvasHeight.toIntOrNull() ?: 240)
    val selectedZone = state.zones.firstOrNull { it.id == state.selectedZoneId }
    var editingObjectsZoneId by rememberSaveable { mutableStateOf<String?>(null) }
    val objectsEditorZone = state.zones.firstOrNull { it.id == editingObjectsZoneId }
    var resetRequest by rememberSaveable { mutableIntStateOf(0) }
    var showRenameFloor by remember { mutableStateOf(false) }
    var showRenameRoom by remember { mutableStateOf(false) }
    var showCustomColor by remember { mutableStateOf(false) }
    var floorName by rememberSaveable { mutableStateOf(state.name) }
    var roomName by rememberSaveable { mutableStateOf("") }
    var customColorInput by rememberSaveable { mutableStateOf(PlanZonePalette.first()) }

    if (objectsEditorZone != null) {
        ZoneObjectsEditorScreen(
            zone = objectsEditorZone,
            grid = grid,
            onAddObject = { item -> viewModel.onAddObjectToZone(objectsEditorZone.id, item) },
            onMoveObject = { objectId, column, row -> viewModel.onMoveZoneObject(objectsEditorZone.id, objectId, column, row) },
            onRotateObject = { objectId -> viewModel.onRotateZoneObject(objectsEditorZone.id, objectId) },
            onDeleteObject = { objectId -> viewModel.onDeleteZoneObject(objectsEditorZone.id, objectId) },
            onDone = { editingObjectsZoneId = null },
            modifier = modifier
        )
        return
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) { viewModel.consumeSaved(); onSaved("Floor saved") }
    }
    LaunchedEffect(state.setupComplete, state.isSaving) {
        onSaveActionChanged(if (state.setupComplete && !state.isSaving) viewModel::save else null, state.setupComplete && !state.isSaving)
    }
    LaunchedEffect(showCustomColor, selectedZone?.fillColor) {
        if (showCustomColor) {
            customColorInput = selectedZone?.fillColor ?: PlanZonePalette.first()
        }
    }
    BackHandler(enabled = state.isDirty) { onBack() }

    if (showRenameFloor) RenameDialog(
        title = "Rename floor", value = floorName, onValueChange = { floorName = it },
        onConfirm = { viewModel.completeSetup(floorName, state.canvasWidth, state.canvasHeight); showRenameFloor = false },
        onDismiss = { showRenameFloor = false }
    )
    if (showRenameRoom && selectedZone != null) RenameDialog(
        title = "Rename room", value = roomName, onValueChange = { roomName = it },
        onConfirm = { viewModel.onSelectedZoneNameChange(roomName); showRenameRoom = false },
        onDismiss = { showRenameRoom = false }
    )

    Box(modifier.fillMaxSize().background(Color(0xFF090B0C))) {
        Column(Modifier.fillMaxSize()) {
            state.errorBanner?.let { message ->
                Surface(color = Color(0xFF311E22), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message, color = Color(0xFFFFC2CB), modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::clearErrorBanner) { Text("Dismiss", color = Color(0xFFFFC2CB)) }
                    }
                }
            }
            Surface(
                color = Color(0xFF0A0E0F),
                border = BorderStroke(1.dp, Color(0xFF243034)),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .zIndex(2f)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Build your floor", color = AppOnSurface, fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    Text(
                        "Tap a zone to select it. Drag a selected zone to move it. Use a corner handle or enter its size below to resize. Pinch or drag the background to inspect the plan.",
                        color = AppSecondaryText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                    Text("1 large square = 1 m. Small squares = 25 cm.", color = AppPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                EditableFloorPlanBoard(
                    grid = grid, zones = state.zones, selectedZoneId = state.selectedZoneId,
                    isRoomToolActive = false, focusRequestKey = 0,
                    focusZoneId = null, resetRequestKey = resetRequest,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
                    onZoneSelected = { id -> viewModel.onSelectZone(id) },
                    onRoomCreated = { column, row, width, height -> viewModel.onCreateRoom(column, row, width, height) },
                    onZoneGeometryChanged = viewModel::onZoneGeometryChanged,
                    onZoneDelete = { _ -> viewModel.onDeleteSelectedZone() }
                )
                Surface(
                    onClick = { resetRequest++ },
                    color = Color(0xFF151C1E),
                    border = BorderStroke(1.dp, Color(0xFF2A3436)),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CenterFocusStrong, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Center", color = AppOnSurface, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            BottomToolTray(
                selectedZone = selectedZone, grid = grid,
                onAddZone = viewModel::onAddZone,
                onRename = { selectedZone?.let { roomName = it.name; showRenameRoom = true } },
                onColor = viewModel::onSelectedZoneColorChange,
                onCustomColor = { showCustomColor = true },
                onResize = viewModel::onResizeZone,
                onDeselect = { viewModel.onSelectZone(null) },
                onEditObjects = { selectedZone?.let { editingObjectsZoneId = it.id } }
            )
        }

        if (state.isSaving) {
            SavingOverlay()
        }
        if (showCustomColor && selectedZone != null) {
            CustomColorDialog(
                title = "Custom color",
                value = customColorInput,
                onValueChange = { customColorInput = it },
                onConfirm = {
                    normalizeColorInput(customColorInput)?.let { normalized ->
                        viewModel.onSelectedZoneColorChange(normalized)
                        showCustomColor = false
                    }
                },
                onDismiss = { showCustomColor = false }
            )
        }
    }
}

@Composable
private fun FloorSetupScreen(state: PlanEditorUiState, onStart: (String) -> Unit, modifier: Modifier) {
    var name by rememberSaveable { mutableStateOf(state.name) }
    LaunchedEffect(state.name) { if (name.isBlank()) name = state.name }
    Column(
        modifier.fillMaxSize().background(Color(0xFF090B0C)).padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Name your floor", color = AppOnSurface, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text("You can create zones as soon as you enter the plan.", color = AppSecondaryText)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Floor name") }, singleLine = true, colors = fieldColors())
        Spacer(Modifier.weight(1f))
        Button(onClick = { onStart(name) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)) {
            Icon(Icons.Outlined.GridOn, null, tint = Color.Black); Spacer(Modifier.width(8.dp)); Text("Start building", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomToolTray(
    selectedZone: PlanZoneDraft?, grid: FloorGridSpec,
    onAddZone: () -> Unit, onRename: () -> Unit,
    onColor: (String) -> Unit,
    onCustomColor: () -> Unit,
    onResize: (String, Int, Int) -> Unit,
    onDeselect: () -> Unit,
    onEditObjects: () -> Unit
) {
    Surface(color = Color(0xFF121718), shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (selectedZone == null) {
                Surface(
                    onClick = onAddZone,
                    color = AppPrimary,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add zone", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                val geometry = selectedZone.geometry(grid)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(selectedZone.name, color = AppOnSurface, fontWeight = FontWeight.Bold)
                        Text("${formatMeters(geometry.spanColumns)} m x ${formatMeters(geometry.spanRows)} m", color = AppSecondaryText, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactActionChip("Done", onDeselect)
                        CompactActionChip("Rename", onRename)
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanZonePalette.forEach { color ->
                        val active = color == selectedZone.fillColor
                        Surface(onClick = { onColor(color) }, color = color.asZoneColor(), shape = RoundedCornerShape(7.dp), border = BorderStroke(if (active) 2.dp else 0.dp, Color.White), modifier = Modifier.size(28.dp)) {}
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactActionChip("Edit objects", onEditObjects)
                    CompactActionChip("Custom color", onCustomColor)
                }
                ZoneSizeInputs(
                    zoneId = selectedZone.id,
                    widthCells = geometry.spanColumns,
                    heightCells = geometry.spanRows,
                    onResize = onResize
                )
            }
        }
    }
}

@Composable
private fun ZoneSizeInputs(
    zoneId: String,
    widthCells: Int,
    heightCells: Int,
    onResize: (String, Int, Int) -> Unit
) {
    var widthInput by rememberSaveable(zoneId) { mutableStateOf(formatMeters(widthCells)) }
    var heightInput by rememberSaveable(zoneId) { mutableStateOf(formatMeters(heightCells)) }
    val currentWidthMeters = formatMeters(widthCells)
    val currentHeightMeters = formatMeters(heightCells)

    LaunchedEffect(currentWidthMeters) {
        if (widthInput.toFloatOrNull() == null || widthInput != currentWidthMeters) widthInput = currentWidthMeters
    }
    LaunchedEffect(currentHeightMeters) {
        if (heightInput.toFloatOrNull() == null || heightInput != currentHeightMeters) heightInput = currentHeightMeters
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = widthInput,
            onValueChange = { value ->
                widthInput = value
                value.toMetersInCells()?.let { onResize(zoneId, it, heightCells) }
            },
            modifier = Modifier.weight(1f),
            label = { Text("Width (m)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors()
        )
        OutlinedTextField(
            value = heightInput,
            onValueChange = { value ->
                heightInput = value
                value.toMetersInCells()?.let { onResize(zoneId, widthCells, it) }
            },
            modifier = Modifier.weight(1f),
            label = { Text("Height (m)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors()
        )
    }
    Text("Each large square is 1 m. Values can use 0.25 m steps.", color = AppSecondaryText, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
}

private fun String.toMetersInCells(): Int? =
    toFloatOrNull()?.takeIf { it >= 0.25f }?.times(SUBCELLS_PER_METER)?.roundToInt()?.coerceAtLeast(1)

private fun formatMeters(cells: Int): String {
    val metres = cells.toFloat() / SUBCELLS_PER_METER
    return if (metres % 1f == 0f) metres.toInt().toString() else metres.toString()
}

@Composable
private fun SavingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
            .background(Color.Black.copy(alpha = 0.62f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF111618),
            border = BorderStroke(1.dp, Color(0xFF273237)),
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(min = 240.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Saving floor", color = AppOnSurface, fontWeight = FontWeight.SemiBold)
                Text("Updating the floor and zones...", color = AppSecondaryText)
                LinearProgressIndicator(
                    color = AppPrimary,
                    trackColor = AppPrimary.copy(alpha = 0.16f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CustomColorDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val normalized = normalizeColorInput(value)
    val previewColor = normalized?.asZoneColor() ?: AppPrimary
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Use a hex color like #76D6D0 or #FF76D6D0.", color = AppSecondaryText)
                Surface(
                    color = previewColor,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF2D373B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {}
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    label = { Text("Hex color") },
                    isError = value.isNotBlank() && normalized == null,
                    supportingText = {
                        if (value.isNotBlank() && normalized == null) {
                            Text("Enter #RRGGBB or #AARRGGBB", color = Color(0xFFFFB6C0))
                        }
                    },
                    colors = fieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = normalized != null
            ) { Text("Apply", color = if (normalized != null) AppPrimary else AppSecondaryText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CompactActionChip(label: String, onClick: () -> Unit, danger: Boolean = false) {
    Surface(
        onClick = onClick,
        color = if (danger) Color(0xFF28181B) else Color(0xFF1B2224),
        border = BorderStroke(1.dp, if (danger) Color(0xFF5A2E35) else Color(0xFF2C393C)),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.height(36.dp).widthIn(min = 72.dp)
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (danger) Color(0xFFFFB6C0) else AppOnSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RenameDialog(title: String, value: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { OutlinedTextField(value, onValueChange, singleLine = true, label = { Text("Name") }, colors = fieldColors()) }, confirmButton = { TextButton(onClick = onConfirm) { Text("Save", color = AppPrimary) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPrimary, focusedLabelColor = AppPrimary, cursorColor = AppPrimary, focusedTextColor = AppOnSurface, unfocusedTextColor = AppOnSurface, unfocusedBorderColor = AppBorder, unfocusedLabelColor = AppSecondaryText)

private fun normalizeColorInput(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    val withHash = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
    val valid = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
    return if (valid.matches(withHash)) withHash.uppercase() else null
}
