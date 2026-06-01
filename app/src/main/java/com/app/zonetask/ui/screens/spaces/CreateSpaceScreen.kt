package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

private val SPACE_TYPE_OPTIONS    = listOf("House", "Apartment", "Office", "Commercial")
private val ROTATION_TYPE_OPTIONS = listOf("Manual", "Automatic", "Weekly")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceScreen(
    ownerId : Int,
    modifier: Modifier = Modifier,
    onSaved : (successMessage: String) -> Unit = {},
    viewModel: CreateSpaceViewModel = viewModel(
        factory = CreateSpaceViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            ownerId         = ownerId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaved("Space created successfully")
        }
    }

    var spaceTypeExpanded    by remember { mutableStateOf(false) }
    var rotationTypeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (uiState.errorBanner != null) {
            Text(
                text  = uiState.errorBanner!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        FormField(label = "Space name") {
            OutlinedTextField(
                value         = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = outlinedTextFieldColors()
            )
        }

        FormField(label = "Space type") {
            ExposedDropdownMenuBox(
                expanded         = spaceTypeExpanded,
                onExpandedChange = { spaceTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value         = uiState.spaceType,
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon  = {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = AppSecondaryText)
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded         = spaceTypeExpanded,
                    onDismissRequest = { spaceTypeExpanded = false }
                ) {
                    SPACE_TYPE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option) },
                            onClick = {
                                viewModel.onSpaceTypeChange(option)
                                spaceTypeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        FormField(label = "Rotation type") {
            ExposedDropdownMenuBox(
                expanded         = rotationTypeExpanded,
                onExpandedChange = { rotationTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value         = uiState.rotationType,
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon  = {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = AppSecondaryText)
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded         = rotationTypeExpanded,
                    onDismissRequest = { rotationTypeExpanded = false }
                ) {
                    ROTATION_TYPE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option) },
                            onClick = {
                                viewModel.onRotationTypeChange(option)
                                rotationTypeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier             = Modifier.fillMaxWidth(),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = "Require proof to complete tasks?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked         = uiState.requireProof,
                onCheckedChange = viewModel::onRequireProofChange,
                colors          = CheckboxDefaults.colors(checkedColor = AppPrimary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick  = { /* plan creation - pending */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary.copy(alpha = 0.85f))
        ) {
            Text(
                text       = "CREATE PLAN",
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
                style      = MaterialTheme.typography.labelLarge
            )
        }

        Button(
            onClick  = viewModel::createSpace,
            enabled  = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color    = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text       = "Save",
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun FormField(
    label  : String,
    content: @Composable () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        content()
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AppPrimary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor     = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor   = MaterialTheme.colorScheme.onBackground,
    cursorColor          = AppPrimary
)
