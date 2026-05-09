package com.app.zonetask.ui.screens.tasks

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.R
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

private val CardColor = Color(0xFF181818)
private val TopBarColor = Color(0xFF141414)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    userId: Int,
    onCreateTask: (spaceId: Int) -> Unit = {},
    onEditTask: (spaceId: Int, taskId: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(
        factory = TasksViewModelFactory(userId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        uiState = uiState,
        onSpaceSelected = viewModel::selectSpace,
        onRetry = viewModel::retrySelectedSpace,
        onCreateTask = onCreateTask,
        onEditTask = onEditTask,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksContent(
    uiState: TasksUiState,
    onSpaceSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onCreateTask: (spaceId: Int) -> Unit,
    onEditTask: (spaceId: Int, taskId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpacePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(modifier = modifier.fillMaxSize()) {

        // ── Custom top bar ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TopBarColor)
                .padding(start = 20.dp, end = 4.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tareas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (uiState.selectedSpaceId != null) {
                IconButton(onClick = { onCreateTask(uiState.selectedSpaceId!!) }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Nueva tarea",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // ── Space combobox ────────────────────────────────────────────────
        Surface(
            onClick = { if (uiState.spaces.isNotEmpty()) showSpacePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = AppSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, AppBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Seleccionar espacio",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppSecondaryText
                    )
                    Text(
                        text = uiState.selectedSpaceName.ifBlank { "Elige un espacio" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (uiState.selectedSpaceName.isBlank()) AppSecondaryText
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_caret_down),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
            }
        }

        // ── Main content ─────────────────────────────────────────────────
        when {
            uiState.isLoadingSpaces && uiState.spaces.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Cargando...",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.errorMessage != null && uiState.spaces.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onRetry) {
                            Text(text = "Reintentar", color = AppPrimary)
                        }
                    }
                }
            }

            uiState.isLoadingTasks -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Cargando tareas...",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.taskErrorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.taskErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onRetry) {
                            Text(text = "Reintentar", color = AppPrimary)
                        }
                    }
                }
            }

            uiState.zoneGroups.isEmpty() && uiState.selectedSpaceId != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay tareas en este espacio.",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(
                        uiState.zoneGroups,
                        key = { group -> "${group.zoneId ?: "none"}-${group.zoneName}" }
                    ) { group ->
                        ZoneSection(
                            group = group,
                            spaceId = uiState.selectedSpaceId,
                            onEditTask = onEditTask
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }

    // ── Space picker bottom sheet ─────────────────────────────────────────
    if (showSpacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSpacePicker = false },
            sheetState = sheetState,
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Seleccionar espacio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                uiState.spaces.forEach { space ->
                    SpacePickerItem(
                        space = space,
                        isSelected = space.spaceId == uiState.selectedSpaceId,
                        onSelect = {
                            onSpaceSelected(space.spaceId)
                            showSpacePicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpacePickerItem(
    space: Space,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        color = if (isSelected) AppPrimary.copy(alpha = 0.10f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = space.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) AppPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ZoneSection(
    group: ZoneTaskGroupUiState,
    spaceId: Int?,
    onEditTask: (spaceId: Int, taskId: Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.zoneName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.2.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = "${group.tasks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = AppPrimary.copy(alpha = 0.12f),
                    labelColor = AppPrimary
                ),
                border = BorderStroke(1.dp, AppPrimary.copy(alpha = 0.28f))
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            group.tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onEditClick = {
                        onEditTask(spaceId ?: task.task.spaceId, task.task.taskId)
                    }
                )
            }
        }
    }
}

private fun String.stripSeconds(): String {
    val parts = split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else this
}

@Composable
private fun TaskCard(
    task: TaskItemUiState,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1 – title + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onEditClick) {
                        Text(
                            text = "Editar",
                            color = AppPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = task.statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (task.statusLabel) {
                                    "Completada" -> AppPrimary
                                    "En progreso" -> Color(0xFF8BB7FF)
                                    "Pendiente" -> Color(0xFFE0B35A)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (task.statusLabel) {
                                "Completada" -> AppPrimary.copy(alpha = 0.16f)
                                "En progreso" -> Color(0xFF304E7B)
                                "Pendiente" -> Color(0xFF3A2F16)
                                else -> AppSurface
                            },
                            labelColor = when (task.statusLabel) {
                                "Completada" -> AppPrimary
                                "En progreso" -> Color(0xFF8BB7FF)
                                "Pendiente" -> Color(0xFFE0B35A)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        border = null
                    )
                }
            }

            task.assignees.firstOrNull()?.let { assignee ->
                Text(
                    text = "Asignado a ${assignee.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } ?: Text(
                text = "Sin asignados visibles",
                style = MaterialTheme.typography.bodyMedium,
                color = AppSecondaryText
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = when (task.dueStatusKey) {
                        "overdue" -> Color(0xFFE57373)
                        "upcoming" -> AppPrimary
                        "completed" -> AppPrimary
                        else -> AppSecondaryText
                    },
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = task.dueLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (task.dueStatusKey) {
                        "overdue" -> Color(0xFFE57373)
                        "upcoming" -> AppPrimary
                        "completed" -> AppPrimary
                        else -> AppSecondaryText
                    }
                )
            }

            task.task.scheduledTime?.takeIf { it.isNotBlank() }?.let { time ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = AppSecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = time.stripSeconds(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppSecondaryText
                    )
                }
            }
        }
    }
}
