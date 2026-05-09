package com.app.zonetask.ui.screens.tasks

import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.domain.model.Space

data class TaskAssigneeUiState(
    val userId: Int,
    val displayName: String
)

data class TaskItemUiState(
    val task: TaskResponse,
    val zoneName: String,
    val assignees: List<TaskAssigneeUiState> = emptyList(),
    val statusLabel: String = "Pendiente",
    val dueLabel: String = "Sin fecha límite",
    val dueStatusKey: String = "none",
    val completionAssignmentId: Int? = null,
    val canComplete: Boolean = false
)

data class ZoneTaskGroupUiState(
    val zoneId: Int?,
    val zoneName: String,
    val tasks: List<TaskItemUiState> = emptyList()
)

data class TasksUiState(
    val isLoadingSpaces: Boolean = false,
    val isLoadingTasks: Boolean = false,
    val deletingTaskId: Int? = null,
    val spaces: List<Space> = emptyList(),
    val selectedSpaceId: Int? = null,
    val selectedSpaceName: String = "",
    val zoneGroups: List<ZoneTaskGroupUiState> = emptyList(),
    val errorMessage: String? = null,
    val taskErrorMessage: String? = null,
    val completionError: String? = null,
    val completingAssignmentId: Int? = null
)
