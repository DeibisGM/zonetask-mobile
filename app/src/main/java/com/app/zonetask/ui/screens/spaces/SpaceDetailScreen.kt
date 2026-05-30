package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
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

private const val ROLE_OWNER = "owner"
private const val ROLE_ADMIN = "admin"

@Composable
fun SpaceDetailScreen(
    spaceId: Int,
    userId: Int,
    modifier: Modifier = Modifier,
    refreshTrigger   : Boolean  = false,
    onRefreshHandled : () -> Unit = {},
    onEditClick      : (Int) -> Unit = {},
    onDeleteSuccess  : () -> Unit = {},
    onNavigateToPermissions: (Int) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onOpenPlansClick : () -> Unit = {},
    viewModel: SpaceDetailViewModel = viewModel(
        factory = SpaceDetailViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId = spaceId,
            userId  = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadSpace()
            viewModel.loadTasks()
            onRefreshHandled()
        }
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            viewModel.consumeDeleteSuccess()
            onDeleteSuccess()
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
                        viewModel.deleteSpace() //
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppPrimary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text  = UserMessages.Spaces.LOADING,
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
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
                    space           = uiState.space!!,
                    userRole        = uiState.userRole,
                    tasks           = uiState.tasks,
                    tasksLoading    = uiState.tasksLoading,
                    tasksError      = uiState.tasksError,
                    completionError = uiState.completionError,
                    onCompleteTask  = viewModel::completeAssignment,
                    onNavigateToPermissions = { onNavigateToPermissions(spaceId) },
                    onCreateTaskClick = onCreateTaskClick,
                    onOpenPlansClick  = onOpenPlansClick,
                    modifier        = Modifier.padding(bottom = 88.dp)
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

                    Button(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !uiState.isDeleting,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(50),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                color     = Color.White,
                                modifier  = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
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
}

@Composable
private fun SpaceDetailContent(
    space: Space,
    userRole: String,
    tasks: List<SpaceTaskUiState>,
    tasksLoading: Boolean,
    tasksError: String?,
    completionError: String?,
    onCompleteTask: (Int) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onCreateTaskClick: () -> Unit,
    onOpenPlansClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canViewPermissions = userRole == ROLE_OWNER || userRole == ROLE_ADMIN

    LazyColumn(
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                text  = "Tareas del espacio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        completionError?.let { error ->
            item {
                Text(
                    text  = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        when {
            tasksLoading -> {
                item {
                    Text(
                        text  = "Cargando tareas...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSecondaryText
                    )
                }
            }

            tasksError != null -> {
                item {
                    Text(
                        text  = tasksError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            tasks.isEmpty() -> {
                item {
                    Text(
                        text  = "No hay tareas todavía en este espacio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSecondaryText
                    )
                }
            }

            else -> {
                items(tasks, key = { it.task.taskId }) { task ->
                    TaskRow(
                        task            = task,
                        onCompleteClick = {
                            task.completionAssignmentId?.let(onCompleteTask)
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick  = onCreateTaskClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Outlined.PlaylistAdd,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Text(
                    text     = "Crear otra tarea",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Floor-plan entry point — available to all space members.
        item {
            Surface(
                onClick = onOpenPlansClick,
                shape   = RoundedCornerShape(16.dp),
                color   = MaterialTheme.colorScheme.surface,
                border  = BorderStroke(1.dp, AppPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.GridView,
                        contentDescription = null,
                        tint               = AppPrimary,
                        modifier           = Modifier.size(20.dp)
                    )
                    Text(
                        text     = "Planos del espacio",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = AppPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector        = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint               = AppPrimary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (canViewPermissions) {
            item {
                Surface(
                    onClick = onNavigateToPermissions,
                    shape   = RoundedCornerShape(16.dp),
                    color   = MaterialTheme.colorScheme.surface,
                    border  = BorderStroke(1.dp, AppPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.AdminPanelSettings,
                            contentDescription = null,
                            tint               = AppPrimary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text     = UserMessages.SpaceDetail.PERMISSIONS_BUTTON,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = AppPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector        = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint               = AppPrimary,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: SpaceTaskUiState, onCompleteClick: () -> Unit) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = task.task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            task.task.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text  = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppSecondaryText
                )
            }
            Text(
                text  = "Frecuencia: ${task.task.frequency}",
                style = MaterialTheme.typography.labelMedium,
                color = AppSecondaryText
            )
            if (task.canComplete && task.completionAssignmentId != null) {
                TextButton(onClick = onCompleteClick) {
                    Text(
                        text  = "Completar",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppPrimary
                    )
                }
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = when (task.dueStatusKey) {
                        "overdue"   -> Color(0xFFE57373)
                        "upcoming"  -> AppPrimary
                        "completed" -> AppPrimary
                        else        -> AppSecondaryText
                    },
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text  = task.dueLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (task.dueStatusKey) {
                        "overdue"   -> Color(0xFFE57373)
                        "upcoming"  -> AppPrimary
                        "completed" -> AppPrimary
                        else        -> AppSecondaryText
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
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