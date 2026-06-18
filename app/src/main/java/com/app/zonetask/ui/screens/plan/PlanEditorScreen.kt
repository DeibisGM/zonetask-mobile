package com.app.zonetask.ui.screens.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.EditableFloorPlanBoard
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

@Composable
fun PlanEditorScreen(
    spaceId: Int,
    planId: Int? = null,
    modifier: Modifier = Modifier,
    onSaved: (message: String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: PlanEditorViewModel = viewModel(
        factory = PlanEditorViewModelFactory(
            floorPlanRepository = AppContainer.floorPlanRepository,
            zoneRepository = AppContainer.zoneRepository,
            spaceId = spaceId,
            planId = planId
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onSaved("Plano guardado correctamente")
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = state.isDirty || state.isSaving) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("¿Salir sin guardar?") },
            text = { Text("Hay cambios en el plano y en las zonas. Si sales ahora se perderán.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) {
                    Text("Descartar", color = AppPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF050505), Color(0xFF0A0E10), Color(0xFF050505))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.errorBanner != null) {
                Surface(
                    color = Color(0xFF24181B),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF523039))
                ) {
                    Text(
                        text = state.errorBanner!!,
                        color = Color(0xFFF1C0C7),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }

            Surface(
                color = AppSurface,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, AppBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (planId == null) "Nuevo plano" else "Editar plano",
                                color = AppOnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${state.zones.size} zonas activas",
                                color = AppSecondaryText
                            )
                        }

                        Button(
                            onClick = viewModel::onAddZone,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Nueva zona",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nombre del plano") },
                        shape = RoundedCornerShape(18.dp),
                        colors = editorFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.canvasWidth,
                            onValueChange = viewModel::onCanvasWidthChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Ancho") },
                            shape = RoundedCornerShape(18.dp),
                            colors = editorFieldColors()
                        )
                        OutlinedTextField(
                            value = state.canvasHeight,
                            onValueChange = viewModel::onCanvasHeightChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Alto") },
                            shape = RoundedCornerShape(18.dp),
                            colors = editorFieldColors()
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AppPrimary)
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(text = "Cargando plano", color = AppSecondaryText)
                    }
                } else {
                    val canvasWidth = state.canvasWidth.toFloatOrNull()?.coerceAtLeast(1f) ?: 1000f
                    val canvasHeight = state.canvasHeight.toFloatOrNull()?.coerceAtLeast(1f) ?: 800f

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(30.dp),
                        color = AppSurface,
                        border = BorderStroke(1.dp, AppBorder)
                    ) {
                        EditableFloorPlanBoard(
                            worldWidth = canvasWidth,
                            worldHeight = canvasHeight,
                            zones = state.zones,
                            selectedZoneId = state.selectedZoneId,
                            modifier = Modifier.fillMaxSize(),
                            onZoneSelected = viewModel::onSelectZone,
                            onZoneDragged = viewModel::onZoneDrag,
                            onZoneResized = viewModel::onZoneResize
                        )
                    }
                }
            }

            Surface(
                color = AppSurface,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, AppBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val selectedZone = state.zones.firstOrNull { it.id == state.selectedZoneId }

                    if (selectedZone != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Zona seleccionada",
                                color = AppOnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Arrástrala sobre el tablero o ajusta su tamaño con el tirador.",
                                color = AppSecondaryText
                            )
                        }

                        OutlinedTextField(
                            value = selectedZone.name,
                            onValueChange = viewModel::onSelectedZoneNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Nombre de la zona") },
                            shape = RoundedCornerShape(18.dp),
                            colors = editorFieldColors()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Color",
                                color = AppOnSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PlanZonePalette.forEach { color ->
                                    val selected = selectedZone.fillColor == color
                                    Surface(
                                        onClick = { viewModel.onSelectedZoneColorChange(color) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = color.asZoneColor(),
                                        border = BorderStroke(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) AppPrimary else Color.Transparent
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.size(34.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (selected) {
                                                Text(
                                                    text = "✓",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        Text(
                            text = "Color activo: ${selectedZone.fillColor}",
                            color = AppSecondaryText
                        )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Opacidad", color = AppOnSurface, fontWeight = FontWeight.Medium)
                            Slider(
                                value = selectedZone.opacity,
                                onValueChange = viewModel::onSelectedZoneOpacityChange,
                                valueRange = 0.35f..1f
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                onClick = viewModel::onDuplicateSelectedZone,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    tint = AppPrimary
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Duplicar")
                            }
                            TextButton(
                                onClick = viewModel::onDeleteSelectedZone,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFE07A88)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Eliminar")
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Sin selección",
                                color = AppOnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Selecciona una zona para editar su nombre y su color.",
                                color = AppSecondaryText
                            )
                        }
                    }

                    Divider(color = AppBorder)

                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving && !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (planId == null) "Guardar plano" else "Guardar cambios",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun editorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppPrimary,
    unfocusedBorderColor = AppBorder,
    focusedTextColor = AppOnSurface,
    unfocusedTextColor = AppOnSurface,
    focusedLabelColor = AppPrimary,
    unfocusedLabelColor = AppSecondaryText,
    cursorColor = AppPrimary
)
