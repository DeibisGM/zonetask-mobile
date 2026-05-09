package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.domain.model.Space

data class ZoneTaskGroupUiState(
    val zoneId: Int?,
    val zoneName: String,
    val tasks: List<TaskResponse> = emptyList()
)

data class SpaceDetailUiState(
    val isLoading: Boolean = false,
    val space: Space? = null,
    val tasksLoading: Boolean = false,
    val taskGroups: List<ZoneTaskGroupUiState> = emptyList(),
    val tasksError: String? = null,
    val errorBanner: String? = null
)
