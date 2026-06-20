package com.app.zonetask.ui.screens.plan

import com.app.zonetask.domain.model.FloorPlanTemplate

data class TemplateSelectUiState(
    val templates: List<FloorPlanTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
