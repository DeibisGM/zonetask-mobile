package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.components.ScreenLoadingState
import com.app.zonetask.ui.components.ScreenStateCard
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
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
    refreshTrigger: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    onEditClick: (Int) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    onNavigateToPermissions: (Int) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onOpenPlansClick: () -> Unit = {},
    onOpenCompletedTasksClick: () -> Unit = {},
    onOpenRotationHistoryClick: () -> Unit = {},
    onOpenStatisticsMenuClick: () -> Unit = {},
    viewModel: SpaceDetailViewModel = viewModel(
        factory = SpaceDetailViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId = spaceId,
            userId = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val canManageSpace = uiState.userRole == ROLE_OWNER || uiState.userRole == ROLE_ADMIN
    val activeTaskCount = uiState.tasks.size
    val overdueTaskCount = uiState.tasks.count { it.dueStatusKey == "overdue" }

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
            text = { Text("This removes the space and everything inside it. The action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSpace()
                    }
                ) {
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
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppBackground)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ScreenLoadingState(modifier = Modifier.fillMaxWidth(), lines = 4)
            }
        }

        uiState.errorBanner != null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppBackground)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ScreenStateCard(
                    title = "Could not load space",
                    message = uiState.errorBanner ?: "Try again.",
                    actionText = "Retry",
                    onAction = viewModel::loadSpace
                )
            }
        }

        uiState.space != null -> {
            val space = uiState.space!!

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppBackground)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        SpaceHeroCard(
                            space = space,
                            userRole = uiState.userRole,
                            activeTaskCount = activeTaskCount,
                            overdueTaskCount = overdueTaskCount
                        )
                    }

                    item {
                        DetailSectionHeader(
                            title = "Quick actions",
                            subtitle = "Everything you need is visible and in one place."
                        )
                    }

                    item {
                        ActionRowCard(
                            title = "New task",
                            subtitle = "Create work in this space.",
                            icon = Icons.Outlined.Add,
                            onClick = onCreateTaskClick
                        )
                    }

                    item {
                        ActionRowCard(
                            title = "Floor plans",
                            subtitle = "Open and edit plans for this space.",
                            icon = Icons.Outlined.GridView,
                            onClick = onOpenPlansClick
                        )
                    }

                    item {
                        ActionRowCard(
                            title = "Members & roles",
                            subtitle = if (canManageSpace) "Invite people and adjust permissions." else "Owner and admins only.",
                            icon = Icons.Outlined.AdminPanelSettings,
                            onClick = { if (canManageSpace) onNavigateToPermissions(spaceId) },
                            enabled = canManageSpace
                        )
                    }

                    item {
                        ActionRowCard(
                            title = "Completed tasks",
                            subtitle = "Review finished work in this space.",
                            icon = Icons.Outlined.CheckCircle,
                            onClick = onOpenCompletedTasksClick
                        )
                    }

                    item {
                        ActionRowCard(
                            title = "Rotation history",
                            subtitle = "See how assignments moved over time.",
                            icon = Icons.Outlined.Repeat,
                            onClick = onOpenRotationHistoryClick
                        )
                    }

                    item {
                        ActionRowCard(
                            title = "Reports",
                            subtitle = "Open statistics and space insights.",
                            icon = Icons.Outlined.BarChart,
                            onClick = onOpenStatisticsMenuClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    item {
                        DetailSectionHeader(
                            title = "Tasks",
                            subtitle = "Active work for this space, with clear due state and completion."
                        )
                    }

                    when {
                        uiState.completionError != null -> {
                            item {
                                ScreenStateCard(
                                    title = "Could not complete task",
                                    message = uiState.completionError ?: "Try again.",
                                    actionText = "Retry",
                                    onAction = viewModel::loadTasks
                                )
                            }
                        }

                        uiState.tasksLoading -> {
                            item {
                                ScreenLoadingState(modifier = Modifier.fillMaxWidth(), lines = 3)
                            }
                        }

                        uiState.tasksError != null -> {
                            item {
                                ScreenStateCard(
                                    title = "Tasks could not load",
                                    message = uiState.tasksError ?: "Try again.",
                                    actionText = "Retry",
                                    onAction = viewModel::loadTasks
                                )
                            }
                        }

                        uiState.tasks.isEmpty() -> {
                            item {
                                ScreenStateCard(
                                    title = "No tasks yet",
                                    message = "Create the first task when you want work to start in this space.",
                                    actionText = "New task",
                                    onAction = onCreateTaskClick
                                )
                            }
                        }

                        else -> {
                            items(uiState.tasks, key = { it.task.taskId }) { task ->
                                TaskRowCard(
                                    task = task,
                                    isCompleting = uiState.completingAssignmentId == task.completionAssignmentId,
                                    onComplete = {
                                        task.completionAssignmentId?.let(viewModel::completeAssignment)
                                    }
                                )
                            }
                        }
                    }

                    if (canManageSpace) {
                        item {
                            DetailSectionHeader(
                                title = "Administration",
                                subtitle = "Only owners and admins can change these settings."
                            )
                        }

                        item {
                            ActionRowCard(
                                title = "Edit space",
                                subtitle = "Adjust name, type, description, and cover.",
                                icon = Icons.Outlined.Edit,
                                onClick = { onEditClick(spaceId) }
                            )
                        }

                        item {
                            DangerRowCard(
                                title = "Delete space",
                                subtitle = "Remove the space and all of its content.",
                                icon = Icons.Outlined.Delete,
                                onClick = { showDeleteDialog = true }
                            )
                        }
                    } else {
                        item {
                            ScreenStateCard(
                                title = "Read only settings",
                                message = "Only owners and admins can edit the space, manage roles, or delete it."
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpaceHeroCard(
    space: Space,
    userRole: String,
    activeTaskCount: Int,
    overdueTaskCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = AppCardElevated,
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppPrimary.copy(alpha = 0.14f)
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = space.name.take(1).uppercase(),
                            color = AppPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = space.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = space.spaceType,
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                RoleBadge(role = userRole)
            }

            Text(
                text = space.description?.takeIf { it.isNotBlank() } ?: "No description yet.",
                color = AppSecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoPill(label = "Tasks", value = activeTaskCount.toString())
                InfoPill(label = "Overdue", value = overdueTaskCount.toString())
                InfoPill(label = "Role", value = userRole.ifBlank { "member" }.replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
private fun DetailSectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = AppSecondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ActionRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (enabled) AppCardElevated else AppSurface,
        border = BorderStroke(1.dp, if (enabled) AppBorder else AppBorder.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (enabled) AppPrimary.copy(alpha = 0.14f) else AppSecondaryText.copy(alpha = 0.08f)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) AppPrimary else AppSecondaryText.copy(alpha = 0.45f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else AppSecondaryText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = if (enabled) AppSecondaryText else AppSecondaryText.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DangerRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AppError.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, AppError.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppError.copy(alpha = 0.14f)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = AppError)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = AppError,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppError,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TaskRowCard(
    task: SpaceTaskUiState,
    isCompleting: Boolean,
    onComplete: () -> Unit
) {
    val statusColor = when (task.dueStatusKey) {
        "overdue" -> Color(0xFFE57373)
        "upcoming" -> AppPrimary
        else -> AppSecondaryText
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppSurface,
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = task.task.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    task.task.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(
                            text = description,
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                task.task.scheduledTime?.let { time ->
                    Text(
                        text = time.take(5),
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoTonePill(
                    label = task.dueLabel,
                    tone = statusColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (task.task.zoneId != null) {
                    InfoTonePill(
                        label = "Zone ${task.task.zoneId}",
                        tone = AppSecondaryText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (task.canComplete && task.completionAssignmentId != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onComplete,
                        enabled = !isCompleting,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AppPrimary
                            )
                        } else {
                            Text("Complete", color = AppPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppSurface,
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = AppSecondaryText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoTonePill(
    label: String,
    tone: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tone.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = tone,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RoleBadge(role: String) {
    val tone = when (role.lowercase()) {
        ROLE_OWNER -> AppPrimary
        ROLE_ADMIN -> AppPrimary.copy(alpha = 0.86f)
        else -> AppSecondaryText
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tone.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.22f))
    ) {
        Text(
            text = role.ifBlank { "member" }.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = tone,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
