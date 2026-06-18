package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.AuthPrimaryButton
import com.app.zonetask.ui.components.AuthScreenShell
import com.app.zonetask.ui.components.AuthStatusMessage
import com.app.zonetask.ui.components.AuthTextField
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

private val SPACE_TYPE_OPTIONS = listOf(
    "House",
    "Apartment",
    "Office",
    "Commercial"
)

private val ROTATION_TYPE_OPTIONS = listOf(
    "Manual",
    "Automatic",
    "Weekly"
)

@Composable
fun CreateSpaceScreen(
    ownerId: Int,
    modifier: Modifier = Modifier,
    onSaved: (successMessage: String) -> Unit = {},
    onContinueToPlan: (spaceId: Int) -> Unit = {},
    viewModel: CreateSpaceViewModel = viewModel(
        factory = CreateSpaceViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            ownerId = ownerId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var continueToPlanRequested by remember { mutableStateOf(false) }
    var saveOnlyRequested by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess, uiState.createdSpaceId) {
        if (!uiState.isSuccess) return@LaunchedEffect

        val createdSpaceId = uiState.createdSpaceId
        if (continueToPlanRequested && createdSpaceId != null) {
            continueToPlanRequested = false
            saveOnlyRequested = false
            onContinueToPlan(createdSpaceId)
        } else if (saveOnlyRequested) {
            saveOnlyRequested = false
            continueToPlanRequested = false
            onSaved("Space created successfully")
        }
    }

    var spaceTypeExpanded by remember { mutableStateOf(false) }
    var rotationTypeExpanded by remember { mutableStateOf(false) }

    AuthScreenShell(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            AuthStatusMessage(
                message = uiState.errorBanner,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    androidx.compose.material3.Surface(
                        color = AppCardElevated,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, AppBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            AuthTextField(
                                value = uiState.name,
                                onValueChange = viewModel::onNameChange,
                                label = "Space name",
                                placeholder = "E.g. Home, office, apartment",
                                error = if (uiState.name.isNotBlank() && uiState.name.length < 3) {
                                    "The name must be at least 3 characters long"
                                } else null
                            )

                            AuthDropdownField(
                                label = "Space type",
                                value = uiState.spaceType.ifBlank { "Select a type" },
                                expanded = spaceTypeExpanded,
                                onExpandedChange = { spaceTypeExpanded = it },
                                options = SPACE_TYPE_OPTIONS,
                                onOptionSelected = {
                                    viewModel.onSpaceTypeChange(it)
                                    spaceTypeExpanded = false
                                }
                            )

                            AuthDropdownField(
                                label = "Rotation type",
                                value = uiState.rotationType.ifBlank { "Select rotation" },
                                expanded = rotationTypeExpanded,
                                onExpandedChange = { rotationTypeExpanded = it },
                                options = ROTATION_TYPE_OPTIONS,
                                onOptionSelected = {
                                    viewModel.onRotationTypeChange(it)
                                    rotationTypeExpanded = false
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Require proof to complete tasks?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppOnSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Turn it on when you want evidence before closing tasks.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppSecondaryText,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = uiState.requireProof,
                                    onCheckedChange = viewModel::onRequireProofChange
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuthPrimaryButton(
                    text = "Create space and continue",
                    onClick = {
                        continueToPlanRequested = true
                        saveOnlyRequested = false
                        viewModel.createSpace()
                    },
                    loading = uiState.isLoading
                )

                OutlinedButton(
                    onClick = {
                        continueToPlanRequested = false
                        saveOnlyRequested = true
                        viewModel.createSpace()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppBorder)
                ) {
                    Text(
                        text = "Save space only",
                        color = AppOnSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthDropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppOnSurface.copy(alpha = 0.85f),
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .onGloballyPositioned { coordinates ->
                        anchorWidthPx = coordinates.size.width
                    },
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppSecondaryText
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = AppBorder,
                    disabledContainerColor = Color(0xFF262626),
                    disabledTextColor = AppOnSurface,
                    disabledTrailingIconColor = AppSecondaryText,
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    focusedContainerColor = Color(0xFF262626),
                    unfocusedContainerColor = Color(0xFF262626),
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    cursorColor = AppPrimary
                )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onExpandedChange(true) }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .width(with(density) { anchorWidthPx.toDp() })
                    .background(AppSurface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = AppOnSurface,
                                fontWeight = if (value == option) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}
