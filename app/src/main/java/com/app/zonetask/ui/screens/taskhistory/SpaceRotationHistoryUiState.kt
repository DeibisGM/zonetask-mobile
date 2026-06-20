package com.app.zonetask.ui.screens.taskhistory

import com.app.zonetask.data.remote.dto.RotationHistoryResponse

data class RotationHistoryFilterOption(
    val id: Int,
    val label: String
)

data class SpaceRotationHistoryUiState(
    val isLoading: Boolean = true,
    val items: List<RotationHistoryResponse> = emptyList(),
    val errorMessage: String? = null,
    val taskOptions: List<RotationHistoryFilterOption> = emptyList(),
    val zoneOptions: List<RotationHistoryFilterOption> = emptyList(),
    val userOptions: List<RotationHistoryFilterOption> = emptyList(),
    val selectedTaskId: Int? = null,
    val selectedZoneId: Int? = null,
    val selectedUserId: Int? = null,
    val dateFrom: String = "",
    val dateTo: String = "",
    val triggerReason: String = "all"
)
