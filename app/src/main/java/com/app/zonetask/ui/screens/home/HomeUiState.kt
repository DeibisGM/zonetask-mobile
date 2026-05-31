package com.app.zonetask.ui.screens.home

import com.app.zonetask.domain.model.FloorPlan
import com.app.zonetask.domain.model.Space

data class HomeTaskItem(
    val taskId: Int,
    val title: String,
    val scheduledTime: String?,
    val zoneName: String,
    val assigneeName: String?,
    val dueLabel: String,
    val dueStatusKey: String
)

data class HomeUiState(
    val spaceName: String = "",
    val plans: List<FloorPlan> = emptyList(),
    val activePlan: FloorPlan? = null,
    val pendingTasks: List<HomeTaskItem> = emptyList(),
    val userSpaces: List<Space> = emptyList(),
    val currentSpaceId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isPlansLoading: Boolean = false
)
