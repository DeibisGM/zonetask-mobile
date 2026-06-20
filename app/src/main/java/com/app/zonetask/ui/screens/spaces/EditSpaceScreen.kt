package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.app.zonetask.ui.components.ScreenLoadingState
import com.app.zonetask.ui.components.ScreenStateCard
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

private val SPACE_TYPE_OPTIONS = listOf("House", "Apartment", "Office", "Commercial")

@Composable
fun EditSpaceScreen(
    spaceId: Int,
    modifier: Modifier = Modifier,
    onSaved: (successMessage: String) -> Unit = {},
    viewModel: EditSpaceViewModel = viewModel(
        factory = EditSpaceViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId = spaceId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSaved("Space updated")
    }

    if (uiState.isLoadingData) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ScreenLoadingState(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), lines = 2)
        }
        return
    }

    var spaceTypeExpanded by remember { mutableStateOf(false) }
    var spaceTypeAnchorWidthPx by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenStateCard(
            title = "Space settings",
            message = "Keep the space simple: name, type, and cover image if you need it."
        )

        if (uiState.errorBanner != null) {
            ScreenStateCard(
                title = "Save issue",
                message = uiState.errorBanner!!,
                actionText = "Retry",
                onAction = viewModel::loadSpace
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppCardElevated,
            border = BorderStroke(1.dp, AppBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormField(label = "Space name") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = editTextFieldColors()
                    )
                }

                FormField(label = "Description") {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = editTextFieldColors(),
                        placeholder = { Text(text = "Optional", color = AppSecondaryText) }
                    )
                }

                FormField(label = "Space type") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.spaceType,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    spaceTypeAnchorWidthPx = coordinates.size.width
                                },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppSecondaryText
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = editTextFieldColors()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { spaceTypeExpanded = true }
                        )

                        DropdownMenu(
                            expanded = spaceTypeExpanded,
                            onDismissRequest = { spaceTypeExpanded = false },
                            modifier = Modifier.width(with(androidx.compose.ui.platform.LocalDensity.current) { spaceTypeAnchorWidthPx.toDp() })
                        ) {
                            SPACE_TYPE_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.onSpaceTypeChange(option)
                                        spaceTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                FormField(label = "Cover image URL") {
                    OutlinedTextField(
                        value = uiState.coverImageUrl,
                        onValueChange = viewModel::onCoverImageUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = editTextFieldColors(),
                        placeholder = { Text(text = "Optional", color = AppSecondaryText) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(
            onClick = viewModel::updateSpace,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save changes",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        content()
    }
}

@Composable
private fun editTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppPrimary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    cursorColor = AppPrimary
)
