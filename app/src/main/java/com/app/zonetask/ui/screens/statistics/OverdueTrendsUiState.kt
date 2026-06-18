package com.app.zonetask.ui.screens.statistics

import com.app.zonetask.data.remote.dto.OverdueTrendsResponse

enum class TrendInterval(val apiValue: String, val label: String) {
    DAY("day", "Día"),
    WEEK("week", "Semana"),
    MONTH("month", "Mes")
}

enum class TrendGroupBy(val label: String) {
    TIME("Tiempo"),
    USER("Usuario"),
    ZONE("Zona")
}

data class OverdueTrendsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val trends: OverdueTrendsResponse? = null,
    val errorMessage: String? = null,
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val dateFrom: String = "",
    val dateTo: String = "",
    val interval: TrendInterval? = null,
    val groupBy: TrendGroupBy = TrendGroupBy.TIME
)
