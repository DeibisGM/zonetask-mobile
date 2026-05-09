package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppIconTint
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SpaceDetailScreen(
    spaceId: Int,
    modifier: Modifier = Modifier,
    onCreateTaskClick: () -> Unit = {},
    viewModel: SpaceDetailViewModel = viewModel(
        factory = SpaceDetailViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId         = spaceId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            SpaceDetailContent(
                space    = uiState.space!!,
                tasks = uiState.tasks,
                tasksLoading = uiState.tasksLoading,
                tasksError = uiState.tasksError,
                onCreateTaskClick = onCreateTaskClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SpaceDetailContent(
    space: Space,
    tasks: List<TaskResponse>,
    tasksLoading: Boolean,
    tasksError: String?,
    onCreateTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text  = space.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
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

        item {
            Text(
                text = "Tareas del espacio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when {
            tasksLoading -> {
                item {
                    Text(
                        text = "Cargando tareas...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSecondaryText
                    )
                }
            }

            tasksError != null -> {
                item {
                    Text(
                        text = tasksError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            tasks.isEmpty() -> {
                item {
                    Text(
                        text = "No hay tareas todavía en este espacio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSecondaryText
                    )
                }
            }

            else -> {
                items(tasks) { task ->
                    TaskRow(task = task)
                }
            }
        }

        item {
            Button(
                onClick = onCreateTaskClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlaylistAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Crear otra tarea",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskResponse) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            task.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppSecondaryText
                )
            }
            Text(
                text = "Frecuencia: ${task.frequency}",
                style = MaterialTheme.typography.labelMedium,
                color = AppSecondaryText
            )
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
