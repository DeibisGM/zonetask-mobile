package com.app.zonetask.ui.screens.taskhistory

import com.app.zonetask.data.remote.dto.RotationHistoryResponse

data class TaskRotationHistoryUiState(
    val isLoading: Boolean = true,
    val items: List<RotationHistoryResponse> = emptyList(),
    val errorMessage: String? = null,
    val taskTitle: String = "",
    val taskDescription: String? = null,
    val isTaskRotating: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val triggerReason: String = "all"
)
