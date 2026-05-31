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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

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
            title = { Text("Delete space") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteSpace()
                }) {
                    Text("Delete", color = AppError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppPrimary)
            }
        }

        uiState.errorBanner != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorBanner!!, color = AppSecondaryText)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.loadSpace() }) {
                        Text("Retry", color = AppPrimary)
                    }
                }
            }
        }

        uiState.space != null -> {
            val space = uiState.space!!
            val canViewPermissions = uiState.userRole == ROLE_OWNER || uiState.userRole == ROLE_ADMIN

            Column(modifier = modifier.fillMaxSize()) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Space header
                    item {
                        Text(
                            text = space.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Info card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppSurface),
                            border = BorderStroke(1.dp, AppBorder)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                DetailRow(
                                    icon = Icons.Outlined.Category,
                                    label = "Type",
                                    value = space.spaceType
                                )
                                HorizontalDivider(color = AppBorder)
                                DetailRow(
                                    icon = Icons.Outlined.Info,
                                    label = "Description",
                                    value = space.description ?: "No description"
                                )
                            }
                        }
                    }

                    // Plans row
                    item {
                        Surface(
                            onClick = onOpenPlansClick,
                            shape = RoundedCornerShape(14.dp),
                            color = AppSurface,
                            border = BorderStroke(1.dp, AppBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.GridView, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Floor Plans",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Outlined.ChevronRight, null, tint = AppSecondaryText, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Permissions row
                    if (canViewPermissions) {
                        item {
                            Surface(
                                onClick = { onNavigateToPermissions(spaceId) },
                                shape = RoundedCornerShape(14.dp),
                                color = AppSurface,
                                border = BorderStroke(1.dp, AppBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.AdminPanelSettings, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Roles & Permissions",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Outlined.ChevronRight, null, tint = AppSecondaryText, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Tasks header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = onCreateTaskClick) {
                                Text("+ New", color = AppPrimary)
                            }
                        }
                    }

                    // Completion error
                    uiState.completionError?.let { err ->
                        item {
                            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Task list
                    when {
                        uiState.tasksLoading -> {
                            item {
                                Text("Loading...", color = AppSecondaryText)
                            }
                        }

                        uiState.tasksError != null -> {
                            item {
                                Text(uiState.tasksError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        uiState.tasks.isEmpty() -> {
                            item {
                                Text("No tasks in this space yet.", color = AppSecondaryText)
                            }
                        }

                        else -> {
                            items(uiState.tasks, key = { it.task.taskId }) { task ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                                    border = BorderStroke(1.dp, AppBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = task.task.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        task.task.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                            Text(desc, style = MaterialTheme.typography.bodySmall, color = AppSecondaryText)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.AccessTime, null,
                                                tint = when (task.dueStatusKey) {
                                                    "overdue" -> Color(0xFFE57373)
                                                    "upcoming" -> AppPrimary
                                                    else -> AppSecondaryText
                                                },
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(task.dueLabel, style = MaterialTheme.typography.labelSmall,
                                                color = when (task.dueStatusKey) {
                                                    "overdue" -> Color(0xFFE57373)
                                                    "upcoming" -> AppPrimary
                                                    else -> AppSecondaryText
                                                }
                                            )
                                        }
                                        if (task.canComplete && task.completionAssignmentId != null) {
                                            TextButton(
                                                onClick = { task.completionAssignmentId?.let(viewModel::completeAssignment) }
                                            ) {
                                                Text("Complete", color = AppPrimary, style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) } // room for bottom buttons
                }

                // Bottom actions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppSurface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEditClick(spaceId) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                        ) {
                            Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EDIT", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            enabled = !uiState.isDeleting,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppError)
                        ) {
                            if (uiState.isDeleting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Outlined.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("DELETE", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = AppSecondaryText)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppSecondaryText)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
