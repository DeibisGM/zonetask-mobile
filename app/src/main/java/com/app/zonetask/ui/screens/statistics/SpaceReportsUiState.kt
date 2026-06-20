package com.app.zonetask.ui.screens.statistics

import com.app.zonetask.data.remote.dto.UserSpaceReportsResponse

enum class SpaceReportSortBy(val apiValue: String, val label: String) {
    COMPLETION_RATE("completion_rate", "Tasa de Finalización"),
    TOTAL_ASSIGNED("total_assigned", "Total Asignadas"),
    NAME("name", "Nombre")
}

data class SpaceReportsUiState(
    val isLoading: Boolean = true,
    val reports: UserSpaceReportsResponse? = null,
    val errorMessage: String? = null,
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val dateFrom: String = "",
    val dateTo: String = "",
    val sortBy: SpaceReportSortBy = SpaceReportSortBy.COMPLETION_RATE
)
