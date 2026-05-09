package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppIconTint
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SpaceDetailScreen(
    spaceId: Int,
    modifier: Modifier = Modifier,
    refreshTrigger   : Boolean  = false,
    onRefreshHandled : () -> Unit = {},  
    onEditClick   : (Int) -> Unit = {},
    onDeleteSuccess: () -> Unit   = {},
    viewModel: SpaceDetailViewModel = viewModel(
        factory = SpaceDetailViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId         = spaceId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadSpace()
            onRefreshHandled()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Eliminar espacio") },
            text    = { Text("¿Estás seguro de que quieres eliminar este espacio? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteSuccess()
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text  = UserMessages.Spaces.LOADING,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        uiState.errorBanner != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text      = uiState.errorBanner!!,
                        color     = AppSecondaryText,
                        style     = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { viewModel.loadSpace() }) {
                        Text(
                            text  = UserMessages.TAP_TO_RETRY_SUFFIX.trim(),
                            color = AppPrimary
                        )
                    }
                }
            }
        }

        uiState.space != null -> {
            Box(modifier = modifier.fillMaxSize()) {
                SpaceDetailContent(
                    space    = uiState.space!!,
                    modifier = Modifier.padding(bottom = 88.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = { onEditClick(spaceId) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(50),
                        border   = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text       = "EDITAR",
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground,
                            style      = MaterialTheme.typography.labelLarge
                        )
                    }

                    // ELIMINAR
                    Button(
                        onClick  = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(50),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text(
                            text       = "ELIMINAR",
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            style      = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpaceDetailContent(
    space: Space,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text  = space.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, AppBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {

                DetailRow(
                    icon  = Icons.Outlined.Category,
                    label = UserMessages.SpaceDetail.TYPE_LABEL,
                    value = space.spaceType
                )

                HorizontalDivider(color = AppBorder)

                DetailRow(
                    icon  = Icons.Outlined.Info,
                    label = UserMessages.SpaceDetail.DESC_LABEL,
                    value = space.description ?: UserMessages.SpaceDetail.NO_DESCRIPTION
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(18.dp),
            tint               = AppIconTint
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppSecondaryText
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}