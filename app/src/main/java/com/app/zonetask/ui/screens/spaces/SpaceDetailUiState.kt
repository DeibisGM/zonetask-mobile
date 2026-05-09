package com.app.zonetask.ui.screens.spaces
import com.app.zonetask.domain.model.Space
import com.app.zonetask.data.remote.dto.TaskResponse

data class SpaceDetailUiState(
    val isLoading: Boolean = false,
    val space: Space? = null,
    val tasksLoading: Boolean = false,
    val tasks: List<TaskResponse> = emptyList(),
    val tasksError: String? = null,
    val errorBanner: String? = null
)
