package com.app.zonetask.ui.screens.statistics

import com.app.zonetask.data.remote.dto.UserStatisticsResponse

enum class StatsPeriod(val apiValue: String, val label: String) {
    WEEK("week", "Week"),
    MONTH("month", "Month"),
    YEAR("year", "Year"),
    CUSTOM("custom", "Custom")
}

data class IndividualStatisticsUiState(
    val isLoading: Boolean = true,
    val statistics: UserStatisticsResponse? = null,
    val errorMessage: String? = null,
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val dateFrom: String = "",
    val dateTo: String = ""
)
