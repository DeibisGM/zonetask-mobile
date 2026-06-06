package com.app.zonetask.ui.screens.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.FloorPlanCanvas
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

/**
 * Full-screen plan editor: name + canvas dimensions at the top,
 * the interactive blank canvas in the centre, and a Save button at the bottom.
 * Matches the reference design (dark background, teal accent).
 *
 * @param spaceId     Space this plan belongs to.
 * @param planId      null = create new plan; non-null = edit existing.
 * @param onSaved     Called after a successful save, with a feedback message.
 * @param onBack      Closes the screen.
 */
@Composable
fun PlanEditorScreen(
    spaceId:  Int,
    planId:   Int?    = null,
    modifier: Modifier = Modifier,
    onSaved:  (message: String) -> Unit = {},
    onBack:   () -> Unit = {},
    viewModel: PlanEditorViewModel = viewModel(
        factory = PlanEditorViewModelFactory(
            repository = AppContainer.floorPlanRepository,
            spaceId    = spaceId,
            planId     = planId
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Show success feedback then pop.
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onSaved("Plan saved")
        }
    }

    // Intercept system back when there are unsaved changes.
    var showDiscardDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = state.isDirty) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title   = { Text("Discard changes?") },
            text    = { Text("You have unsaved changes. They'll be lost if you leave now.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // ── Top panel: plan metadata fields ─────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .background(AppSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.errorBanner != null) {
                Text(
                    text  = state.errorBanner!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value         = state.name,
                onValueChange = viewModel::onNameChange,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                label         = { Text("Plan name") },
                shape         = RoundedCornerShape(12.dp),
                colors        = planFieldColors()
            )

            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value         = state.canvasWidth,
                    onValueChange = viewModel::onCanvasWidthChange,
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    label         = { Text("Width") },
                    suffix        = { Text("px") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = planFieldColors()
                )
                OutlinedTextField(
                    value         = state.canvasHeight,
                    onValueChange = viewModel::onCanvasHeightChange,
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    label         = { Text("Height") },
                    suffix        = { Text("px") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = planFieldColors()
                )
            }
        }

        // ── Canvas: blank grid — fills remaining vertical space ─────────────
        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            } else {
                val canvasWidth  = state.canvasWidth.toFloatOrNull()?.coerceAtLeast(1f)  ?: 1000f
                val canvasHeight = state.canvasHeight.toFloatOrNull()?.coerceAtLeast(1f) ?: 800f

                FloorPlanCanvas(
                    worldWidth  = canvasWidth,
                    worldHeight = canvasHeight,
                    modifier    = Modifier.fillMaxSize()
                )
            }
        }

        // ── Save button ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick  = viewModel::save,
                enabled  = !state.isSaving && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = AppOnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Outlined.Save,
                        contentDescription = null,
                        tint               = AppOnPrimary,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Save plan",
                        color      = AppOnPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun planFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AppPrimary,
    unfocusedBorderColor = Color(0xFF2C2C2C),
    focusedLabelColor    = AppPrimary,
    unfocusedLabelColor  = AppSecondaryText,
    focusedTextColor     = Color.White,
    unfocusedTextColor   = Color.White,
    cursorColor          = AppPrimary
)
