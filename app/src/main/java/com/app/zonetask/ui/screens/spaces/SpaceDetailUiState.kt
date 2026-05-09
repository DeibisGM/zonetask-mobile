package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.domain.model.Space

data class SpaceDetailUiState(
    val isLoading: Boolean = false,
    val space: Space? = null,
    val userRole: String = "",
    val tasks: List<TaskResponse> = emptyList(),
    val tasksLoading: Boolean = false,
    val tasksError: String? = null,
    val errorBanner: String? = null
)
