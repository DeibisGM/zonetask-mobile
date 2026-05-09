package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.domain.model.Space

data class SpaceTaskUiState(
    val task: TaskResponse,
    val dueLabel: String = "Sin fecha límite",
    val dueStatusKey: String = "none",
    val completionAssignmentId: Int? = null,
    val canComplete: Boolean = false
)

data class SpaceDetailUiState(
    val isLoading: Boolean = false,
    val space: Space? = null,
    val userRole: String = "",
    val tasks: List<SpaceTaskUiState> = emptyList(),
    val tasksLoading: Boolean = false,
    val tasksError: String? = null,
    val completionError: String? = null,
    val completingAssignmentId: Int? = null,
    val errorBanner: String? = null
)
