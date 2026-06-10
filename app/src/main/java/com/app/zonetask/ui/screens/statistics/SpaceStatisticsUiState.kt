package com.app.zonetask.ui.screens.statistics

import com.app.zonetask.data.remote.dto.SpaceStatisticsResponse

data class SpaceStatisticsUiState(
    val isLoading: Boolean = true,
    val statistics: SpaceStatisticsResponse? = null,
    val errorMessage: String? = null,
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val dateFrom: String = "",
    val dateTo: String = ""
)
