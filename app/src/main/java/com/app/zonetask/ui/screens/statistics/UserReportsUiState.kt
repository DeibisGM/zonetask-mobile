package com.app.zonetask.ui.screens.statistics

import com.app.zonetask.data.remote.dto.SpaceUserReportsResponse

enum class ReportSortBy(val apiValue: String, val label: String) {
    COMPLETION_RATE("completion_rate", "Completion Rate"),
    TOTAL_ASSIGNED("total_assigned", "Total Assigned")
}

data class UserReportsUiState(
    val isLoading: Boolean = true,
    val reports: SpaceUserReportsResponse? = null,
    val errorMessage: String? = null,
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val dateFrom: String = "",
    val dateTo: String = "",
    val sortBy: ReportSortBy = ReportSortBy.COMPLETION_RATE
)
