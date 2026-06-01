package com.app.zonetask.ui.screens.plan

import com.app.zonetask.domain.model.FloorPlan

data class PlanListUiState(
    val plans:       List<FloorPlan> = emptyList(),
    val isLoading:   Boolean         = false,
    val errorBanner: String?         = null
)
